package com.example.eveosample.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.DetectFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.services.rekognition.model.Landmark
import com.example.eveosample.R
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.hypot

class BeautyFragment : Fragment(R.layout.fragment_beauty) {

    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var tvRecommendations: TextView

    private val PICK_IMAGE_REQUEST = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.imageView)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        tvRecommendations = view.findViewById(R.id.tvRecommendations)

        btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                imageView.setImageURI(it)

                //image to Bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                val imageWidth = bitmap.width
                val imageHeight = bitmap.height

                //bitmap to ByteArray
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                analyzeFaceWithRekognition(imageBytes, imageWidth, imageHeight)
            }
        }
    }

    private fun analyzeFaceWithRekognition(imageBytes: ByteArray, imageWidth: Int, imageHeight: Int) {
        Thread {
            try {
                val rekognitionClient = AmazonRekognitionClient(AWSMobileClient.getInstance())
                val request = DetectFacesRequest()
                    .withImage(Image().withBytes(ByteBuffer.wrap(imageBytes)))
                    .withAttributes("ALL")
                val result = rekognitionClient.detectFaces(request)

                if (result.faceDetails.isNotEmpty()) {
                    val faceDetail = result.faceDetails[0]
                    val recommendations = generateDetailedRecommendations(faceDetail, imageWidth, imageHeight)
                    activity?.runOnUiThread {
                        tvRecommendations.text = recommendations
                    }
                } else {
                    activity?.runOnUiThread {
                        tvRecommendations.text = "No faces detected."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    tvRecommendations.text = "Error: ${e.message}"
                }
            }
        }.start()
    }

    // Generate detailed beauty recommendations based on facial attributes and calculated metrics
    private fun generateDetailedRecommendations(
        faceDetail: com.amazonaws.services.rekognition.model.FaceDetail,
        imageWidth: Int,
        imageHeight: Int
    ): String {
        val builder = StringBuilder("Detailed Beauty Recommendations:\n\n")

        // 1. Face Shape Analysis using the face bounding box
        val faceBox = faceDetail.boundingBox
        val faceWidth = faceBox.width * imageWidth
        val faceHeight = faceBox.height * imageHeight
        val aspectRatio = faceWidth / faceHeight
        builder.append("- Face aspect ratio: ${"%.2f".format(aspectRatio)}. ")
        when {
            aspectRatio < 0.9 -> builder.append("Your face appears longer; contouring can help balance your features.\n")
            aspectRatio in 0.9..1.1 -> builder.append("Your face shape is balanced; a natural look will enhance your beauty.\n")
            else -> builder.append("Your face appears wider; shading techniques may help slim your features.\n")
        }

        // 2. Eye Analysis: Calculate interocular distance
        val leftEye = getLandmark(faceDetail.landmarks, "eyeLeft")
        val rightEye = getLandmark(faceDetail.landmarks, "eyeRight")
        if (leftEye != null && rightEye != null) {
            val interocularDistance = distanceBetween(leftEye, rightEye, imageWidth, imageHeight)
            builder.append("- Interocular distance: ")
            if (interocularDistance / faceWidth < 0.3) {
                builder.append("Your eyes are relatively close; try bright eyeshadows to create a more open look.\n")
            } else {
                builder.append("Your eye spacing is ideal.\n")
            }
        }

        // 3. Lip Analysis: Calculate mouth width
        val mouthLeft = getLandmark(faceDetail.landmarks, "mouthLeft")
        val mouthRight = getLandmark(faceDetail.landmarks, "mouthRight")
        if (mouthLeft != null && mouthRight != null) {
            val mouthWidth = distanceBetween(mouthLeft, mouthRight, imageWidth, imageHeight)
            builder.append("- Mouth width: ")
            if (mouthWidth / faceWidth < 0.4) {
                builder.append("Your lips appear narrow; using a lip liner and gloss can enhance fullness.\n")
            } else {
                builder.append("Your lips are naturally full; a bold lip color may accentuate them.\n")
            }
        }

        // 4. Symmetry Analysis: Basic evaluation using left and right eye positions and nose as midline reference
        if (leftEye != null && rightEye != null) {
            val nose = getLandmark(faceDetail.landmarks, "nose")
            if (nose != null) {
                val midline = nose.x * imageWidth
                val leftDeviation = abs(leftEye.x * imageWidth - midline)
                val rightDeviation = abs(rightEye.x * imageWidth - midline)
                val symmetryScore = 1 - (abs(leftDeviation - rightDeviation) / midline)
                builder.append("- Facial symmetry score: ${"%.2f".format(symmetryScore * 100)}%\n")

                if (symmetryScore < 0.8) {
                    builder.append("You might benefit from contouring to enhance balance.\n")
                } else {
                    builder.append("Your facial symmetry is excellent.\n")
                }
            }
        }

        // 5. Additional Refinements
        if (faceDetail.beard?.value == true) {
            builder.append("- Your beard adds character; keeping it well-groomed will maintain a sharp appearance.\n")
        } else {
            builder.append("- A clean-shaven look may emphasize your facial features more clearly.\n")
        }
        if (faceDetail.mustache?.value == true) {
            builder.append("- A well-maintained mustache can accentuate your facial structure.\n")
        }

        // Emotion-based suggestion
        if (faceDetail.emotions.isNotEmpty()) {
            val dominantEmotion = faceDetail.emotions.maxByOrNull { it.confidence }?.type ?: "NEUTRAL"
            when (dominantEmotion.uppercase()) {
                "HAPPY" -> builder.append("- Your happy expression is vibrant; try playful, bright makeup to match your mood.\n")
                "SAD" -> builder.append("- A gentle, soft makeup look might brighten your overall appearance.\n")
                "ANGRY" -> builder.append("- Consider a bold and balanced makeup style to complement your intensity.\n")
                else -> builder.append("- A balanced makeup style will naturally enhance your unique expression.\n")
            }
        }

        builder.append("\nFor further refinement, consult a professional makeup artist for personalized contouring and highlighting techniques based on your unique facial geometry.")
        return builder.toString()
    }

    // Helper function to get a landmark by type
    private fun getLandmark(landmarks: List<Landmark>, type: String): Landmark? {
        return landmarks.find { it.type.equals(type, ignoreCase = true) }
    }

    // Helper function to calculate Euclidean distance between two landmarks
    private fun distanceBetween(lm1: Landmark, lm2: Landmark, imageWidth: Int, imageHeight: Int): Double {
        val x1 = lm1.x * imageWidth
        val y1 = lm1.y * imageHeight
        val x2 = lm2.x * imageWidth
        val y2 = lm2.y * imageHeight
        return hypot(x2 - x1, y2 - y1).toDouble()
    }
}
