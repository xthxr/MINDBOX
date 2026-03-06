PLACE YOUR TFLITE MODEL FILES HERE
====================================

1. mobileBERT_embedding.tflite
   - Download from: https://www.kaggle.com/models/google/mobilebert/tfLite
   - Or use TF Hub: https://tfhub.dev/tensorflow/lite-model/mobilebert/1/default/1
   - Used by: ml/EmbeddingModelManager.kt
   - Required for semantic search to work

2. bert_ner_quant.tflite  (optional)
   - Download from: https://www.kaggle.com/models/tensorflow/bert/tfLite
   - Or quantize your own NER model with TFLite Model Maker
   - Used by: nlp/ner/NERModelRunner.kt
   - Optional: the app gracefully falls back to rule-based NLP if absent

After placing the files here, verify the filenames match exactly
(case-sensitive on Android):
  - mobileBERT_embedding.tflite
  - bert_ner_quant.tflite
