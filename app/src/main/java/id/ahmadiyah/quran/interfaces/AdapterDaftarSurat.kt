package id.ahmadiyah.quran.interfaces

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import id.ahmadiyah.quran.R

class AdapterDaftarSurat(internal var mContext: Context, internal var mCursor: Cursor) : RecyclerView.Adapter<AdapterDaftarSurat.ViewHolder>() {

    internal var mListener: View.OnClickListener? = null

    private val mCache: Array<Bitmap?>

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        mCache = arrayOfNulls<Bitmap?>(114)

        initializeBitmap()
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        mListener = listener
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var mNamaSurat: TextView
        var mNomorSurat: TextView
        var mInfoSurat: TextView
        var mImageView: ImageView

        init {
            mImageView = v.findViewById<View>(R.id.khat_surat) as ImageView
            mNamaSurat = v.findViewById<View>(R.id.nama_surat) as TextView
            mNomorSurat = v.findViewById<View>(R.id.nomor_surat) as TextView
            mInfoSurat = v.findViewById<View>(R.id.info_surat) as TextView
        }

    }

    override fun getItemCount(): Int {
        return mCursor.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mCursor.moveToPosition(position)

        val nama = mCursor.getString(mCursor.getColumnIndexOrThrow("nama"))
        val formatSurat = mContext.getString(R.string.format_surat)
        holder.mNamaSurat.text = Html.fromHtml(String.format(formatSurat, nama))

        val klasifikasi = mCursor.getInt(mCursor.getColumnIndexOrThrow("klasifikasi"))
        val jumlahAyat = mCursor.getInt(mCursor.getColumnIndexOrThrow("jumlah_ayat"))

        val format: String
        when (klasifikasi) {
            1 -> format = mContext.getString(R.string.format_info_makkiyah)
            2 -> format = mContext.getString(R.string.format_info_madaniyah)
            else -> format = "unknown"
        }

        val info = String.format(format, jumlahAyat)
        holder.mNomorSurat.text = Integer.toString(position + 1)
        holder.mInfoSurat.text = info

        holder.mImageView.setImageBitmap(mCache[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_surat, parent, false)

        v.setOnClickListener(mListener)

        return ViewHolder(v)
    }

    fun initializeBitmap() {
        for (i in 0..9) {
            mCache[i] = getBitmapFromDrawable(getDrawable(i + 1))
        }

        val task = BitmapWorkerTask()
        task.execute()
    }

    private fun getDrawable(position: Int): Drawable {
        val path = String.format("surat_%03d", position)
        val id = mContext.resources.getIdentifier(path, "drawable", mContext.packageName)
        val image = AppCompatResources.getDrawable(mContext, id)
        return image!!
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(330, 124, Bitmap.Config.ALPHA_8)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, -69, 330, 196)
        drawable.draw(canvas)
        return bitmap
    }

    internal inner class BitmapWorkerTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            for (i in 10..113) {
                mCache[i] = getBitmapFromDrawable(getDrawable(i + 1))
            }
            return null
        }
    }
}