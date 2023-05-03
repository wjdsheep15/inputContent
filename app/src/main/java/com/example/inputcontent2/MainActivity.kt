package com.example.inputcontent2

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import com.example.inputcontent2.databinding.ActivityMainBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {



    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String
    lateinit var calendar : Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.setOnClickListener {
           val intent = Intent(this, inputContent::class.java)
            startActivity(intent)
        }

        //gallery request launcher......................
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            try{
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio

                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null

                bitmap?.let {
                    binding.userImageView.setImageBitmap(bitmap)
                } ?: let{
                    Log.d("kkang", "bitmap null")
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }
        }

        binding.galleryButton.setOnClickListener {
            //gallery app........................
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        //camera request launcher.................
        val requestCameraFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            val calRatio = calculateInSampleSize(
                Uri.fromFile(File(filePath)),
                resources.getDimensionPixelSize(R.dimen.imgSize),
                resources.getDimensionPixelSize(R.dimen.imgSize)
            )
            val option = BitmapFactory.Options()
            option.inSampleSize = calRatio
            val bitmap = BitmapFactory.decodeFile(filePath, option)
            bitmap?.let {
                binding.userImageView.setImageBitmap(bitmap)
            }
        }

        binding.cameraButton.setOnClickListener {
            //camera app......................
            //파일 준비...............
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
            filePath = file.absolutePath
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.ch16_provider.fileprovider",
                file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            requestCameraFileLauncher.launch(intent)

        }



        //calendar Date
         val dataPicker = MaterialDatePicker.Builder.datePicker()
             .setTitleText("Select date")
             .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
             .build()

        binding.dataPickerBtn.setOnClickListener {
            dataPicker.show(supportFragmentManager, "Material Date Picker")
            dataPicker.addOnPositiveButtonClickListener { datePicked ->
                val oneDate = datePicked
                if(datePicked!=null){
                    binding.dataPickerText.text = "날짜 : "+ convertLongToDate(oneDate)
                }

            }
        }

        //calendar Range
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select dates")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

        binding.dataRangeBtn.setOnClickListener {
            dateRangePicker.show(supportFragmentManager, "Material Date Range Picker")
            dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
                val startDate = datePicked.first
                val endDate = datePicked.second
                /* Toast.makeText(this, "$startDate $secondDate", Toast.LENGTH_SHORT).show()*/

                if(startDate!=null&&endDate!=null) {
                    binding.dataPickerText.text =
                        "여행 시작 : " + convertLongToDate(startDate) + "\n여행 끝일 : " + convertLongToDate(
                            endDate
                        )
                }
            }
        }

    }


    private fun convertLongToDate(time:Long):String{
        val date = Date(time)
        val format = SimpleDateFormat(
            "yyyy년-MM월-dd일", Locale.getDefault()
        )
        return format.format(date)
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}