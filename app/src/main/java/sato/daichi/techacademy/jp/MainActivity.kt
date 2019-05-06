package sato.daichi.techacademy.jp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mHandler = Handler()
    private var mTimer: Timer? = null
    private var urlList = ArrayList<Uri>() //画像のURLリストを入れる配列
    private var index = 1
    private var status = false // true:再生, false:一時停止

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setControlButtonText()

        //次へボタン
        next_button.setOnClickListener {
            if (!status) {
                if (urlList.size > 0) {
                    // 最後の画像表示時に進むボタンを押したら最初の画像を表示
                    if (index >= urlList.size) {
                        index = 0
                    }
                    //1つ先の画像を表示
                    imageView.setImageURI(urlList.get(index++))
                }
            }
        }

        //戻るボタン
        back_button.setOnClickListener {
            if (!status) {
                if (urlList.size > 0) {
                    // 最初の画像表示時に戻るボタンを押したら最後の画像を表示
                    if (index < 0 ) {
                        index = urlList.size -1;
                    }
                    //1つ後の画像を表示
                    imageView.setImageURI(urlList.get(index--))
                }
            }
        }

        //スライドショー機能
        slide_button.setOnClickListener {
            status = !status;
            setControlButtonText()
            if (status) {
                if (mTimer == null && urlList.size > 0) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                //画像が最後までいったら最初に戻る
                                if (index >= urlList.size) {
                                    index = 0
                                }
                                imageView.setImageURI(urlList.get(index++))
                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで 2秒、ループの間隔を 2秒 に設定
                }
            } else {
                if (mTimer != null) {
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        }
        else {
            getContentsInfo()
        }
    }

    //一時停止/再生のボタン表示切り替え
    // 自動送りの間は、進むボタンと戻るボタンはタップ不可にする
    private  fun setControlButtonText() {
        if (status) {
            slide_button.text = "一時停止"
            next_button.isClickable = false
            back_button.isClickable = false
        }  else {
            slide_button.text = "再生"
            next_button.isClickable = true
            back_button.isClickable = true
        }
    }

    //permissionの権限確認
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    //画像の情報取得
    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                urlList.add(imageUri)
                imageView.setImageURI(urlList.get(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
