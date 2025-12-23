from flask import Flask, request, jsonify
from flask_cors import CORS
import tensorflow as tf
import numpy as np
from PIL import Image
import io
import logging
import cv2
from mtcnn import MTCNN
import google.generativeai as genai
import os
import json
import base64
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

app = Flask(__name__)
CORS(app)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load FaceNet model and MTCNN face detector
logger.info("Loading FaceNet model and MTCNN detector...")
facenet_model = None
face_detector = None

def load_models():
    """Load FaceNet model and MTCNN face detector"""
    global facenet_model, face_detector
    
    try:
        # Load pre-trained FaceNet model (Inception ResNet V1)
        # You can download from: https://github.com/davidsandberg/facenet
        # Or use keras-facenet: pip install keras-facenet
        from keras_facenet import FaceNet
        facenet_model = FaceNet()
        logger.info("✓ FaceNet model loaded successfully")
        
        # Load MTCNN face detector
        face_detector = MTCNN()
        logger.info("✓ MTCNN face detector loaded successfully")
        
    except Exception as e:
        logger.error(f"Error loading models: {str(e)}")
        raise

# Load models on startup
load_models()

# Configure Gemini
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    logger.info("✓ Gemini API configured")
else:
    logger.warning("⚠ GEMINI_API_KEY not set - liveness verification will be disabled")

def preprocess_image(image_array, required_size=(160, 160)):
    """
    Preprocess image for FaceNet model
    Args:
        image_array: numpy array of image
        required_size: target size for FaceNet (160x160)
    Returns:
        preprocessed image array
    """
    # Resize image
    image = Image.fromarray(image_array)
    image = image.resize(required_size)
    face_array = np.asarray(image)
    
    # Normalize pixel values
    face_array = face_array.astype('float32')
    mean, std = face_array.mean(), face_array.std()
    face_array = (face_array - mean) / std
    
    return face_array

def extract_face(image_array, required_size=(160, 160)):
    """
    Extract face from image using MTCNN
    Args:
        image_array: numpy array of image
        required_size: target size for face
    Returns:
        face array and detection confidence
    """
    # Detect faces
    results = face_detector.detect_faces(image_array)
    
    if len(results) == 0:
        return None, 0.0
    
    # Get the face with highest confidence
    results = sorted(results, key=lambda x: x['confidence'], reverse=True)
    x1, y1, width, height = results[0]['box']
    x1, y1 = abs(x1), abs(y1)
    x2, y2 = x1 + width, y1 + height
    
    # Extract face
    face = image_array[y1:y2, x1:x2]
    
    # Resize to required size
    face_image = Image.fromarray(face)
    face_image = face_image.resize(required_size)
    face_array = np.asarray(face_image)
    
    confidence = results[0]['confidence']
    
    return face_array, confidence

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'face-recognition-tensorflow',
        'model': 'FaceNet (Inception ResNet V1)',
        'embedding_dimensions': 512
    }), 200

@app.route('/detect-face', methods=['POST'])
def detect_face():
    """
    Detect if an image contains a face using MTCNN
    Returns: {'face_detected': boolean, 'num_faces': int, 'confidence': float}
    """
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        file = request.files['image']
        
        # Read and convert image
        image_data = file.read()
        image = Image.open(io.BytesIO(image_data))
        
        # Convert to RGB if necessary
        if image.mode != 'RGB':
            image = image.convert('RGB')
        
        # Convert to numpy array
        image_array = np.array(image)
        
        # Detect faces
        results = face_detector.detect_faces(image_array)
        num_faces = len(results)
        
        # Get highest confidence if faces detected
        confidence = 0.0
        if num_faces > 0:
            confidence = max([r['confidence'] for r in results])
        
        logger.info(f"Detected {num_faces} face(s) with max confidence: {confidence:.2f}")
        
        return jsonify({
            'face_detected': num_faces > 0,
            'num_faces': num_faces,
            'confidence': float(confidence),
            'message': f'Found {num_faces} face(s)' if num_faces > 0 else 'No face detected'
        }), 200
        
    except Exception as e:
        logger.error(f"Error detecting face: {str(e)}")
        return jsonify({'error': f'Face detection failed: {str(e)}'}), 500

@app.route('/extract-embedding', methods=['POST'])
def extract_embedding():
    """
    Extract 512-dimensional face embedding using FaceNet
    Returns: {'embedding': list of 512 floats, 'confidence': float}
    """
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        file = request.files['image']
        
        # Read and convert image
        image_data = file.read()
        image = Image.open(io.BytesIO(image_data))
        
        # Convert to RGB if necessary
        if image.mode != 'RGB':
            image = image.convert('RGB')
        
        # Convert to numpy array
        image_array = np.array(image)
        
        # Extract face using MTCNN
        face_array, confidence = extract_face(image_array)
        
        if face_array is None:
            return jsonify({'error': 'No face detected in image'}), 400
        
        # Check confidence threshold
        if confidence < 0.9:
            logger.warning(f"Low face detection confidence: {confidence:.2f}")
        
        # Preprocess face for FaceNet
        face_pixels = preprocess_image(face_array)
        
        # Expand dimensions to match model input (batch_size, height, width, channels)
        face_pixels = np.expand_dims(face_pixels, axis=0)
        
        # Get embedding from FaceNet
        embedding = facenet_model.embeddings(face_pixels)
        
        # Convert to 1D array
        embedding = embedding[0]
        
        # Normalize embedding (L2 normalization)
        embedding = embedding / np.linalg.norm(embedding)
        
        logger.info(f"Successfully extracted {len(embedding)}-dimensional embedding (confidence: {confidence:.2f})")
        
        return jsonify({
            'embedding': embedding.tolist(),
            'dimensions': len(embedding),
            'confidence': float(confidence),
            'model': 'FaceNet (Inception ResNet V1)'
        }), 200
        
    except Exception as e:
        logger.error(f"Error extracting embedding: {str(e)}")
        return jsonify({'error': f'Embedding extraction failed: {str(e)}'}), 500

@app.route('/compare-faces', methods=['POST'])
def compare_faces():
    """
    Compare two face embeddings and return similarity score
    Expects: {'embedding1': [...], 'embedding2': [...]}
    Returns: {'similarity': float, 'distance': float}
    """
    try:
        data = request.get_json()
        
        if 'embedding1' not in data or 'embedding2' not in data:
            return jsonify({'error': 'Both embedding1 and embedding2 are required'}), 400
        
        emb1 = np.array(data['embedding1'])
        emb2 = np.array(data['embedding2'])
        
        if len(emb1) != 512 or len(emb2) != 512:
            return jsonify({'error': 'Embeddings must be 512-dimensional'}), 400
        
        # Calculate Euclidean distance
        distance = np.linalg.norm(emb1 - emb2)
        
        # Calculate cosine similarity
        cosine_similarity = np.dot(emb1, emb2) / (np.linalg.norm(emb1) * np.linalg.norm(emb2))
        
        # Convert to 0-1 range
        similarity = (cosine_similarity + 1) / 2
        
        logger.info(f"Face comparison - Distance: {distance:.4f}, Similarity: {similarity:.4f}")
        
        return jsonify({
            'similarity': float(similarity),
            'cosine_similarity': float(cosine_similarity),
            'euclidean_distance': float(distance),
            'match': distance < 1.0  # FaceNet threshold
        }), 200
        
    except Exception as e:
        logger.error(f"Error comparing faces: {str(e)}")
        return jsonify({'error': f'Face comparison failed: {str(e)}'}), 500

@app.route('/verify-action', methods=['POST'])
def verify_action():
    """
    Verify specific liveness action using Gemini Vision AI
    
    Parameters:
        - image: Image file (multipart/form-data)
        - action: String - "LOOK_STRAIGHT", "BLINK", or "TURN_LEFT"
    
    Returns:
        {
            "verified": true/false,
            "confidence": 0.0-1.0,
            "reason": "Explanation",
            "action": "LOOK_STRAIGHT"
        }
    """
    try:
        if not GEMINI_API_KEY:
            return jsonify({'error': 'Gemini API not configured'}), 503
        
        if 'image' not in request.files:
            return jsonify({'error': 'No image file provided'}), 400
        
        if 'action' not in request.form:
            return jsonify({'error': 'No action specified'}), 400
        
        file = request.files['image']
        action = request.form['action'].upper()
        
        if action not in ['LOOK_STRAIGHT', 'BLINK', 'TURN_LEFT']:
            return jsonify({'error': 'Invalid action. Must be LOOK_STRAIGHT, BLINK, or TURN_LEFT'}), 400
        
        # Read image
        image_data = file.read()
        image = Image.open(io.BytesIO(image_data))
        
        # Convert to RGB if necessary
        if image.mode != 'RGB':
            image = image.convert('RGB')
        
        # Verify with Gemini
        result = verify_with_gemini(image_data, action)
        
        logger.info(f"Action verification for {action}: {result}")
        
        return jsonify({
            'verified': result.get('verified', False),
            'confidence': result.get('confidence', 0.0),
            'reason': result.get('reason', ''),
            'action': action
        }), 200
        
    except Exception as e:
        logger.error(f"Error verifying action: {str(e)}")
        return jsonify({'error': f'Action verification failed: {str(e)}'}), 500

def verify_with_gemini(image_bytes, action):
    """
    Use Gemini Vision AI to verify liveness action
    
    Args:
        image_bytes: Raw image bytes
        action: "LOOK_STRAIGHT", "BLINK", or "TURN_LEFT"
    
    Returns:
        dict: {"verified": bool, "confidence": float, "reason": str}
    """
    try:
        # Define prompts for each action
        prompts = {
            "LOOK_STRAIGHT": """
Analyze this image for eKYC liveness verification (Phase 1: Look Straight).

Check if:
1. There is exactly ONE real human face (not a photo, screen, or mask)
2. The person is looking STRAIGHT at the camera (eyes facing forward)
3. Both eyes are clearly visible and OPEN
4. The face is well-lit and in focus
5. No signs of spoofing:
   - Not a printed photo (check for paper texture, edges)
   - Not a phone/tablet screen (check for pixels, glare)
   - Not a mask or 3D model
   - Not a video playback

Return ONLY a valid JSON object with this exact format:
{
  "verified": true or false,
  "confidence": 0.0 to 1.0,
  "reason": "Brief explanation"
}

Examples:
- If real person looking straight: {"verified": true, "confidence": 0.95, "reason": "Real face detected, looking straight, both eyes open"}
- If looking away: {"verified": false, "confidence": 0.3, "reason": "Face not looking straight at camera"}
- If photo detected: {"verified": false, "confidence": 0.1, "reason": "Spoofing detected: appears to be a printed photo"}
""",
            "BLINK": """
Analyze this image for eKYC liveness verification (Phase 2: Blink Detection).

IMPORTANT: This phase should PASS if the person's eyes are CLOSED, PARTIALLY CLOSED, or SQUINTING.
The goal is to detect ANY eye closure movement, not just a perfect blink.

Check if:
1. There is a real human face
2. The person's eyes show ANY of these signs:
   ✓ Eyes fully closed
   ✓ Eyes partially closed (squinting)
   ✓ Eyelids lowered significantly
   ✓ Eyes narrowed or half-closed
   ✓ Any visible attempt to close eyes
3. The face is still a real person (not a photo or screen)

PASS CRITERIA (be lenient):
- If eyes are closed or partially closed → PASS with high confidence
- If eyes are squinting or narrowed → PASS with medium confidence
- If there's any visible eye closure attempt → PASS

FAIL CRITERIA:
- Only fail if eyes are WIDE OPEN with no closure attempt
- Or if it's clearly a photo/screen

Return ONLY a valid JSON object with this exact format:
{
  "verified": true or false,
  "confidence": 0.0 to 1.0,
  "reason": "Brief explanation"
}

Examples:
- Eyes fully closed: {"verified": true, "confidence": 0.95, "reason": "Eyes fully closed, perfect blink"}
- Eyes partially closed: {"verified": true, "confidence": 0.85, "reason": "Eyes partially closed, blink detected"}
- Eyes squinting: {"verified": true, "confidence": 0.75, "reason": "Eyes squinting, closure detected"}
- Eyes wide open: {"verified": false, "confidence": 0.2, "reason": "Eyes wide open, no blink detected"}
- Photo/screen: {"verified": false, "confidence": 0.1, "reason": "Spoofing detected"}
""",
            "TURN_LEFT": """
Analyze this image for eKYC liveness verification (Phase 3: Turn Left).

IMPORTANT: This phase should PASS if the person's head shows ANY leftward rotation.
The goal is to detect head movement, not a perfect 90-degree turn.

Check if:
1. There is a real human face
2. The person's head shows ANY of these signs of turning LEFT (their left, camera's right):
   ✓ Head rotated left (any degree)
   ✓ Face showing 3/4 view or side profile
   ✓ Left side of face more visible than right
   ✓ Left ear visible or more prominent
   ✓ Face not perfectly centered (shifted right in frame)
   ✓ Any visible leftward head rotation
3. The face is still a real person (not a photo or screen)

PASS CRITERIA (be lenient):
- If head is turned left (any degree) → PASS with high confidence
- If face shows 3/4 view → PASS with high confidence
- If there's any visible leftward rotation → PASS with medium confidence
- Even slight leftward tilt → PASS

FAIL CRITERIA:
- Only fail if face is perfectly straight/centered with NO leftward rotation
- Or if head is turned RIGHT instead
- Or if it's clearly a photo/screen

Return ONLY a valid JSON object with this exact format:
{
  "verified": true or false,
  "confidence": 0.0 to 1.0,
  "reason": "Brief explanation"
}

Examples:
- Head turned left 45°+: {"verified": true, "confidence": 0.95, "reason": "Head clearly turned left, side profile visible"}
- Head turned left slightly: {"verified": true, "confidence": 0.80, "reason": "Head rotated left, movement detected"}
- Face tilted left: {"verified": true, "confidence": 0.70, "reason": "Leftward tilt detected"}
- Face perfectly straight: {"verified": false, "confidence": 0.3, "reason": "No leftward rotation detected"}
- Head turned right: {"verified": false, "confidence": 0.2, "reason": "Head turned right instead of left"}
- Photo/screen: {"verified": false, "confidence": 0.1, "reason": "Spoofing detected"}
"""
        }
        
        # Get prompt for action
        prompt = prompts.get(action)
        if not prompt:
            return {"verified": False, "confidence": 0.0, "reason": "Invalid action"}
        
        # Initialize Gemini model
        model = genai.GenerativeModel('gemini-flash-latest')
        
        # Prepare image for Gemini
        image_part = {
            "mime_type": "image/jpeg",
            "data": base64.b64encode(image_bytes).decode('utf-8')
        }
        
        # Generate content
        response = model.generate_content([prompt, image_part])
        
        # Parse JSON response
        response_text = response.text.strip()
        
        # Remove markdown code blocks if present
        if response_text.startswith('```json'):
            response_text = response_text[7:]
        if response_text.startswith('```'):
            response_text = response_text[3:]
        if response_text.endswith('```'):
            response_text = response_text[:-3]
        
        response_text = response_text.strip()
        
        # Parse JSON
        result = json.loads(response_text)
        
        # Validate response format
        if 'verified' not in result or 'confidence' not in result or 'reason' not in result:
            logger.warning(f"Invalid Gemini response format: {result}")
            return {
                "verified": False,
                "confidence": 0.0,
                "reason": "Invalid response from AI"
            }
        
        return result
        
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse Gemini response as JSON: {str(e)}")
        logger.error(f"Response text: {response_text}")
        return {
            "verified": False,
            "confidence": 0.0,
            "reason": "Failed to parse AI response"
        }
    except Exception as e:
        logger.error(f"Gemini verification error: {str(e)}")
        return {
            "verified": False,
            "confidence": 0.0,
            "reason": f"AI verification failed: {str(e)}"
        }

if __name__ == '__main__':
    logger.info("=" * 60)
    logger.info("Starting Face Recognition Service with TensorFlow FaceNet")
    logger.info("Model: FaceNet (Inception ResNet V1)")
    logger.info("Embedding Dimensions: 512")
    logger.info("Face Detector: MTCNN")
    logger.info("Server: http://0.0.0.0:5000")
    logger.info("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=True)
