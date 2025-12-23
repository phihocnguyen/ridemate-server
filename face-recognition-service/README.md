# Face Recognition Service - TensorFlow FaceNet

Python microservice for face detection and embedding extraction using **TensorFlow** with **FaceNet (Inception ResNet V1)** model.

## Features

- **High-Quality Embeddings**: Native 512-dimensional face embeddings
- **State-of-the-Art Model**: FaceNet (Inception ResNet V1) - one of the most accurate face recognition models
- **Robust Face Detection**: MTCNN (Multi-task Cascaded Convolutional Networks) for precise face detection
- **Production Ready**: Optimized for accuracy and performance

## Model Details

- **Face Recognition**: FaceNet (Inception ResNet V1)
- **Embedding Dimensions**: 512
- **Face Detection**: MTCNN
- **Accuracy**: ~99.6% on LFW dataset
- **Distance Threshold**: < 1.0 for same person

## Setup

### Local Development

1. **Install Python 3.10+**

2. **Install dependencies**:
```bash
cd face-recognition-service
pip install -r requirements.txt
```

3. **Run the service**:
```bash
python app.py
```

The service will start on `http://localhost:5000`

### Docker

Build and run with Docker:
```bash
docker build -t face-recognition-service .
docker run -p 5000:5000 face-recognition-service
```

## API Endpoints

### 1. Health Check
```http
GET /health
```

**Response**:
```json
{
  "status": "healthy",
  "service": "face-recognition-tensorflow",
  "model": "FaceNet (Inception ResNet V1)",
  "embedding_dimensions": 512
}
```

### 2. Detect Face
```http
POST /detect-face
Content-Type: multipart/form-data
```

**Parameters**:
- `image`: Image file

**Response**:
```json
{
  "face_detected": true,
  "num_faces": 1,
  "confidence": 0.9987,
  "message": "Found 1 face(s)"
}
```

### 3. Extract Embedding
```http
POST /extract-embedding
Content-Type: multipart/form-data
```

**Parameters**:
- `image`: Image file

**Response**:
```json
{
  "embedding": [0.123, -0.456, ...],  // 512-dimensional array
  "dimensions": 512,
  "confidence": 0.9987,
  "model": "FaceNet (Inception ResNet V1)"
}
```

### 4. Compare Faces
```http
POST /compare-faces
Content-Type: application/json
```

**Request Body**:
```json
{
  "embedding1": [0.123, -0.456, ...],
  "embedding2": [0.789, -0.012, ...]
}
```

**Response**:
```json
{
  "similarity": 0.95,
  "cosine_similarity": 0.90,
  "euclidean_distance": 0.45,
  "match": true
}
```

## Face Matching Thresholds

- **Euclidean Distance**: < 1.0 for same person
- **Cosine Similarity**: > 0.4 for same person (after normalization to 0-1 range: > 0.7)

## Technical Details

### FaceNet Model
- Pre-trained on VGGFace2 and CASIA-WebFace datasets
- Uses Inception ResNet V1 architecture
- Produces L2-normalized 512-dimensional embeddings
- Optimized for face verification and identification

### MTCNN Face Detector
- Multi-stage cascade of CNNs
- Detects faces and facial landmarks
- Returns confidence scores
- Handles multiple faces and various angles

### Preprocessing
1. Face detection with MTCNN
2. Face alignment and cropping
3. Resize to 160x160 pixels
4. Pixel normalization (mean/std)
5. Embedding extraction
6. L2 normalization

## Performance

- **Face Detection**: ~50-100ms per image
- **Embedding Extraction**: ~100-200ms per image
- **Memory Usage**: ~500MB (model loaded in memory)

## Error Handling

- Returns 400 for invalid inputs or no face detected
- Returns 500 for server errors
- Logs all errors with detailed messages

## Production Recommendations

1. **GPU Support**: For faster inference, use TensorFlow GPU version
2. **Batch Processing**: Process multiple images in batches
3. **Caching**: Cache embeddings for frequently accessed faces
4. **Load Balancing**: Use multiple instances behind a load balancer
5. **Monitoring**: Add Prometheus metrics for monitoring

## Troubleshooting

### Model Loading Issues
If the model fails to load, ensure you have enough memory (at least 2GB RAM).

### Low Confidence Scores
- Ensure good lighting in images
- Face should be clearly visible
- Avoid extreme angles or occlusions

### Slow Performance
- Use GPU-enabled TensorFlow for faster inference
- Reduce image size before sending to API
- Consider using TensorFlow Lite for mobile deployment

## References

- [FaceNet Paper](https://arxiv.org/abs/1503.03832)
- [MTCNN Paper](https://arxiv.org/abs/1604.02878)
- [keras-facenet](https://github.com/nyoki-mtl/keras-facenet)
