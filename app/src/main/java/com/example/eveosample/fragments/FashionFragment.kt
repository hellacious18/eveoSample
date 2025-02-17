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
import com.amazonaws.services.rekognition.model.DetectLabelsRequest
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.services.rekognition.model.Landmark
import com.amazonaws.services.rekognition.model.Label
import com.example.eveosample.R
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.hypot

class FashionFragment : Fragment(R.layout.fragment_fashion) {

    private lateinit var imageView: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var tvRecommendations: TextView

    private val PICK_IMAGE_REQUEST = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.imageViewFashion)
        btnSelectImage = view.findViewById(R.id.btnSelectImageFashion)
        tvRecommendations = view.findViewById(R.id.tvRecommendationsFashion)

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
                // Display the selected image
                imageView.setImageURI(it)

                // Convert image to Bitmap and capture dimensions
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                val imageWidth = bitmap.width
                val imageHeight = bitmap.height

                // Convert bitmap to ByteArray
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                // Analyze image with Rekognition and pass image dimensions
                analyzeImageWithRekognition(imageBytes, imageWidth, imageHeight)
            }
        }
    }

    private fun analyzeImageWithRekognition(imageBytes: ByteArray, imageWidth: Int, imageHeight: Int) {
        Thread {
            try {
                val rekognitionClient = AmazonRekognitionClient(AWSMobileClient.getInstance())
                val request = DetectLabelsRequest()
                    .withImage(Image().withBytes(ByteBuffer.wrap(imageBytes)))
                    .withMaxLabels(20)            // Increase max labels for more details
                    .withMinConfidence(70F)       // Adjust threshold as needed
                val result = rekognitionClient.detectLabels(request)

                val recommendations = generateFashionRecommendations(result.labels, imageWidth, imageHeight)
                activity?.runOnUiThread {
                    tvRecommendations.text = recommendations
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    tvRecommendations.text = "Error: ${e.message}"
                }
            }
        }.start()
    }

    // Generate detailed fashion recommendations based on detected labels and body proportions
    private fun generateFashionRecommendations(
        labels: List<Label>,
        imageWidth: Int,
        imageHeight: Int
    ): String {
        val builder = StringBuilder("Detailed Fashion Recommendations:\n\n")

        // 1. Analyze full-body proportions using the "Person" label instance
        val personInstance = labels.find { it.name.equals("Person", ignoreCase = true) }?.instances?.firstOrNull()
        if (personInstance != null) {
            val bbox = personInstance.boundingBox
            val bodyWidth = bbox.width * imageWidth
            val bodyHeight = bbox.height * imageHeight
            val bodyAspectRatio = bodyHeight / bodyWidth
            builder.append("- Body proportions: width = ${"%.1f".format(bodyWidth)} px, height = ${"%.1f".format(bodyHeight)} px, aspect ratio = ${"%.2f".format(bodyAspectRatio)}.\n")
            when {
                bodyAspectRatio > 2.0 -> builder.append("   Your body appears longer; consider outfits that create horizontal emphasis such as belts, layered tops, or cropped jackets to balance your silhouette.\n")
                bodyAspectRatio < 1.5 -> builder.append("   Your proportions are balanced; versatile outfits that accentuate your natural shape may work well.\n")
                else -> builder.append("   Your body proportions are well-defined; feel free to experiment with different styles and trends.\n")
            }
        } else {
            builder.append("- Full-body proportions could not be determined. Ensure the image clearly shows your full body.\n")
        }

        // 2. Clothing Analysis: Filter clothing-related labels
        val clothingLabels = labels.filter {
            it.name.contains("Clothing", true) ||
                    it.name.contains("Dress", true) ||
                    it.name.contains("T-Shirt", true) ||
                    it.name.contains("Shirt", true) ||
                    it.name.contains("Jeans", true) ||
                    it.name.contains("Pants", true) ||
                    it.name.contains("Jacket", true) ||
                    it.name.contains("Skirt", true) ||
                    it.name.contains("Suit", true)
        }

        if (clothingLabels.isEmpty()) {
            builder.append("\nNo specific clothing items were detected. Ensure the full-body image clearly shows your outfit.\n")
        } else {
            builder.append("\nDetected clothing items:\n")
            clothingLabels.forEach { label ->
                builder.append("   - ${label.name} (confidence: ${"%.1f".format(label.confidence)}%)\n")
            }

            // 3. Provide detailed recommendations based on clothing type
            if (clothingLabels.any { it.name.contains("Dress", true) }) {
                builder.append("\nFor dresses:\n")
                builder.append("   Consider pairing your dress with elegant accessories and matching footwear. A tailored blazer can also create a chic layered look.\n")
            }
            if (clothingLabels.any { it.name.contains("T-Shirt", true) || it.name.contains("Shirt", true) }) {
                builder.append("\nFor casual tops:\n")
                builder.append("   Try layering with a stylish jacket or scarf. Experiment with patterns and textures to add depth to your look.\n")
            }
            if (clothingLabels.any { it.name.contains("Jeans", true) || it.name.contains("Pants", true) }) {
                builder.append("\nFor bottoms like jeans or pants:\n")
                builder.append("   Consider adding a statement belt or switching up your footwear to transform your overall appearance. Tailored fits often enhance the silhouette.\n")
            }
            if (clothingLabels.any { it.name.contains("Jacket", true) || it.name.contains("Suit", true) }) {
                builder.append("\nFor outerwear or formal attire:\n")
                builder.append("   Well-fitted blazers or jackets can elevate your style. Consider accessorizing with a watch or subtle jewelry for a refined look.\n")
            }
        }

        // 4. Additional Recommendations
        builder.append("\nFor personalized advice, consider consulting a stylist for a detailed wardrobe review and to explore trends that suit your body type and lifestyle.")

        return builder.toString()
    }
}
