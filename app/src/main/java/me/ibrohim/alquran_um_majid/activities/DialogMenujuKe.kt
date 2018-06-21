package me.ibrohim.alquran_um_majid.activities

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.models.Posisi
import me.ibrohim.alquran_um_majid.models.Surat

/**
 * Created by ibrohim on 3/15/17.
 */

// Empty constructor required for DialogFragment
class DialogMenujuKe : DialogFragment() {

    private var mViewSurat: Spinner? = null
    private var mViewAyat: EditText? = null
    private var listener: PosisiListener? = null
    private var mPosisi: Posisi? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater
        val inflater = activity.layoutInflater

        val listSurat = listSurat
        val spinnerArrayAdapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_dropdown_item,
                listSurat)

        val layoutView = inflater.inflate(R.layout.fragment_goto, null)
        mViewSurat = layoutView.findViewById<View>(R.id.surat) as Spinner
        mViewSurat!!.adapter = spinnerArrayAdapter
        mViewAyat = layoutView.findViewById<View>(R.id.ayat) as EditText

        builder.setTitle("Menuju ke ayat ...")
        builder.setView(layoutView)
                .setPositiveButton("ok", null)
                .setNegativeButton("cancel") { dialog, id -> this@DialogMenujuKe.dialog.cancel() }

        if (mPosisi != null) {
            mViewSurat!!.setSelection(mPosisi!!.surat - 1)
            mViewAyat!!.setText(Integer.toString(mPosisi!!.ayat))
        }

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog
        val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener { performOkButtonAction() }
    }

    private fun isValid(surat: Int, ayat: Int): Boolean {
        return Surat.getJumlahAyat(surat) >= ayat
    }

    private fun performOkButtonAction() {
        val stringAyat = mViewAyat!!.text.toString()
        val intAyat = Integer.parseInt(stringAyat)
        val intSurat = mViewSurat!!.selectedItemPosition + 1

        if (isValid(intSurat, intAyat)) {
            if (listener != null) listener!!.SetPosisi(Posisi(intSurat, intAyat))
            dialog.dismiss()
        } else {
            mViewAyat!!.error = "Ayat yang dipilih tidak ada."
        }
    }

    private val listSurat: Array<String?>
        get() {
            val listNamaSurat = Surat.listNamaSurat!!
            val result = arrayOfNulls<String>(listNamaSurat.size)

            val format = "%d - %s"
            for (i in listNamaSurat.indices) {
                result[i] = String.format(format, i + 1, listNamaSurat[i])
            }

            return result
        }

    interface PosisiListener {
        fun SetPosisi(posisi: Posisi)
    }

    fun setPosisiListener(_listener: PosisiListener) {
        listener = _listener
    }

    fun setCurrentPosition(posisi: Posisi) {
        mPosisi = posisi
    }
}
