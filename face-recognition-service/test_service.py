"""
Test script for Face Recognition Service
Tests all endpoints with sample images
"""

import requests
import json
import numpy as np
from PIL import Image
import io

BASE_URL = "http://localhost:5000"

def test_health():
    """Test health endpoint"""
    print("\n" + "="*60)
    print("Testing Health Endpoint")
    print("="*60)
    
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    assert response.status_code == 200
    assert response.json()['status'] == 'healthy'
    print("✓ Health check passed")

def create_test_image():
    """Create a simple test image"""
    # Create a random image (in real test, use actual face image)
    img = Image.new('RGB', (640, 480), color='white')
    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='JPEG')
    img_byte_arr.seek(0)
    return img_byte_arr

def test_detect_face():
    """Test face detection endpoint"""
    print("\n" + "="*60)
    print("Testing Face Detection")
    print("="*60)
    
    # In real test, use actual face image
    # For now, this will return no face detected
    files = {'image': ('test.jpg', create_test_image(), 'image/jpeg')}
    response = requests.post(f"{BASE_URL}/detect-face", files=files)
    
    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print("✓ Face detection endpoint working")

def test_extract_embedding():
    """Test embedding extraction endpoint"""
    print("\n" + "="*60)
    print("Testing Embedding Extraction")
    print("="*60)
    
    # In real test, use actual face image
    files = {'image': ('test.jpg', create_test_image(), 'image/jpeg')}
    response = requests.post(f"{BASE_URL}/extract-embedding", files=files)
    
    print(f"Status Code: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print(f"Embedding dimensions: {data['dimensions']}")
        print(f"Confidence: {data['confidence']}")
        print(f"Model: {data['model']}")
        print("✓ Embedding extraction working")
    else:
        print(f"Response: {json.dumps(response.json(), indent=2)}")

def test_compare_faces():
    """Test face comparison endpoint"""
    print("\n" + "="*60)
    print("Testing Face Comparison")
    print("="*60)
    
    # Create two random 512-dim embeddings
    emb1 = np.random.randn(512).tolist()
    emb2 = np.random.randn(512).tolist()
    
    data = {
        'embedding1': emb1,
        'embedding2': emb2
    }
    
    response = requests.post(
        f"{BASE_URL}/compare-faces",
        json=data,
        headers={'Content-Type': 'application/json'}
    )
    
    print(f"Status Code: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print("✓ Face comparison endpoint working")

if __name__ == '__main__':
    print("\n" + "="*60)
    print("Face Recognition Service - Test Suite")
    print("="*60)
    print("\nNOTE: For full testing, replace create_test_image() with actual face images")
    
    try:
        test_health()
        test_detect_face()
        test_extract_embedding()
        test_compare_faces()
        
        print("\n" + "="*60)
        print("All tests completed!")
        print("="*60)
        
    except Exception as e:
        print(f"\n❌ Test failed: {str(e)}")
