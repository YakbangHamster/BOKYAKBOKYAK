package com.example.yakbanghamster

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.yakbanghamster.databinding.ActivityRealtimeDetectionBinding
import com.example.yakbanghamster.yolo.DetectionResult
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.framework.image.BitmapImageBuilder

class RealTimeDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRealtimeDetectionBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var interpreter: Interpreter
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var faceDetector: ObjectDetector
    private var handLandmarker: HandLandmarker? = null

    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var isActive = true
    private var isProcessing = false
    private var frameCounter = 0

    companion object {
        const val MODEL_INPUT_SIZE = 960
        const val NUM_CLASSES = 4
        private const val TAG = "YOLOv8"
    }

    private val labels = arrayOf("mouth", "cup", "pill", "hand")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtimeDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 가이드 모달
        binding.guideDim.visibility = View.VISIBLE
        binding.guideModal.visibility = View.VISIBLE
        binding.guideStartBtn.setOnClickListener {
            binding.guideDim.visibility = View.GONE
            binding.guideModal.visibility = View.GONE
            startCamera()
        }

        binding.switchCameraBtn.setOnClickListener {
            currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera()
        }

        loadModel()
        initHandDetector()
        initFaceDetector()

        inputBuffer = ByteBuffer.allocateDirect(1 * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4)
            .apply { order(ByteOrder.nativeOrder()) }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(MODEL_INPUT_SIZE, MODEL_INPUT_SIZE))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (isActive && !isProcessing) {
                    isProcessing = true
                    try {
                        processImage(imageProxy)
                    } catch (e: Exception) {
                        Log.e(TAG, "Analyzer error", e)
                        imageProxy.close() // 예외 발생 시에도 닫기
                        isProcessing = false
                    }
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, currentCameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadModel() {
        try {
            val options = Interpreter.Options().apply { setUseNNAPI(true) }
            interpreter = Interpreter(loadModelFile("best_float32 (1).tflite"), options)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Model load failed", e)
        }
    }

    private fun loadModelFile(filename: String): ByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun initFaceDetector() {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
        faceDetector = ObjectDetection.getClient(options)
    }

    private fun initHandDetector() {
        try {
            val options = HandLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.5f)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(this, options)
        } catch (e: Exception) {
            Log.e(TAG, "HandLandmarker init error", e)
            handLandmarker = null
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        try {
            frameCounter++
            if (frameCounter % 2 != 0) {
                return
            }

            val bitmap = ImageUtils.imageProxyToBitmap(imageProxy)
            val modelInputBitmap = ImageUtils.scaleBitmap(bitmap, MODEL_INPUT_SIZE)
            inputBuffer.rewind()
            bitmapToFloatBuffer(modelInputBitmap, inputBuffer)

            val outputArray = Array(1) { Array(8) { FloatArray(18900) } }
            interpreter.run(inputBuffer, outputArray)

            val parsed = parseYoloOutput(outputArray)
            val nmsAll = nonMaxSuppression(parsed, 0.55f)
            val finalDetects = filterMaxOnePerClass(nmsAll)

            binding.overlayView.setResults(finalDetects)

            val handCount = detectHandsWithMediaPipe(modelInputBitmap)
            val pill = finalDetects.firstOrNull { it.label == "pill" && it.confidence >= 0.6f }
            val cup = finalDetects.firstOrNull { it.label == "cup" && it.confidence >= 0.6f }
            val isTakingMedicine = pill != null && cup != null && handCount > 0
            Log.d(TAG, "pill=$pill, cup=$cup, handCount=$handCount, taking=$isTakingMedicine")

            bitmap.recycle()
            modelInputBitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Processing error", e)
        } finally {
            imageProxy.close() // 항상 닫기
            isProcessing = false
        }
    }

    private fun bitmapToFloatBuffer(bitmap: Bitmap, buffer: ByteBuffer) {
        buffer.rewind()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (px in pixels) {
            buffer.putFloat(((px shr 16) and 0xFF) / 255f)
            buffer.putFloat(((px shr 8) and 0xFF) / 255f)
            buffer.putFloat((px and 0xFF) / 255f)
        }
    }

    private fun parseYoloOutput(output: Array<Array<FloatArray>>): List<DetectionResult> {
        val list = mutableListOf<DetectionResult>()
        val confidenceThreshold = 0.25f

        for (featureMap in output[0]) {
            var i = 0
            while (i < featureMap.size) {
                val objectness = featureMap[i + 4]
                val classScores = featureMap.copyOfRange(i + 5, i + 5 + NUM_CLASSES)
                val maxClassScore = classScores.maxOrNull() ?: 0f
                val finalConf = objectness * maxClassScore

                if (finalConf >= confidenceThreshold) {
                    val classIndex = classScores.indices.maxBy { classScores[it] }
                    val label = labels[classIndex]

                    val cx = featureMap[i] * MODEL_INPUT_SIZE
                    val cy = featureMap[i + 1] * MODEL_INPUT_SIZE
                    val w = featureMap[i + 2] * MODEL_INPUT_SIZE
                    val h = featureMap[i + 3] * MODEL_INPUT_SIZE

                    list.add(DetectionResult(RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2), label, finalConf))
                }
                i += 5 + NUM_CLASSES
            }
        }
        return list
    }

    private fun nonMaxSuppression(list: List<DetectionResult>, threshold: Float): List<DetectionResult> {
        val sorted = list.sortedByDescending { it.confidence }.toMutableList()
        val picked = mutableListOf<DetectionResult>()
        while (sorted.isNotEmpty()) {
            val current = sorted.removeAt(0)
            picked.add(current)
            val it = sorted.iterator()
            while (it.hasNext()) {
                if (iou(current.rect, it.next().rect) > threshold) it.remove()
            }
        }
        return picked
    }

    private fun filterMaxOnePerClass(detections: List<DetectionResult>): List<DetectionResult> {
        val picked = mutableListOf<DetectionResult>()
        val usedLabels = mutableSetOf<String>()
        for (det in detections.sortedByDescending { it.confidence }) {
            if (det.label !in usedLabels) {
                picked.add(det)
                usedLabels.add(det.label)
            }
            if (usedLabels.size == labels.size) break
        }
        return picked
    }

    private fun iou(a: RectF, b: RectF): Float {
        val inter = maxOf(0f, minOf(a.right, b.right) - maxOf(a.left, b.left)) *
                maxOf(0f, minOf(a.bottom, b.bottom) - maxOf(a.top, b.top))
        val union = a.width() * a.height() + b.width() * b.height() - inter
        return if (union <= 0) 0f else inter / union
    }

    private fun detectHandsWithMediaPipe(bitmap: Bitmap): Int {
        val landmarker = handLandmarker ?: return 0
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = landmarker.detect(mpImage)
        val hands = result.handednesses()
        return hands.count { list ->
            (list.maxByOrNull { it.score() }?.score() ?: 0f) >= 0.5f
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        try { interpreter.close() } catch (_: Exception) {}
        try { handLandmarker?.close() } catch (_: Exception) {}
        cameraExecutor.shutdown()
    }
}
