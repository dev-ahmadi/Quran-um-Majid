package id.ahmadiyah.quran.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.models.Posisi
import id.ahmadiyah.quran.models.Surat

/**
 * Created by ibrohim on 3/15/17.
 */

// Empty constructor required for DialogFragment
class DialogMenujuKe : DialogFragment() {

    private var dropdownSurat: Spinner? = null
    private var ayatEditText: EditText? = null
    private var posisiListener: PosisiListener? = null
    private var posisi: Posisi? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater

        val listSurat = listSurat
        val spinnerArrayAdapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                listSurat)

        val layoutView = inflater.inflate(R.layout.fragment_goto, null)
        dropdownSurat = layoutView.findViewById<View>(R.id.surat) as Spinner
        dropdownSurat!!.adapter = spinnerArrayAdapter
        ayatEditText = layoutView.findViewById<View>(R.id.ayat) as EditText

        builder.setTitle("Menuju ke ayat ...")
        builder.setView(layoutView)
                .setPositiveButton("ok", null)
                .setNegativeButton("cancel") { dialog, id -> this@DialogMenujuKe.dialog?.cancel() }

        val currentPosition = posisi
        if (currentPosition != null) {
            dropdownSurat!!.setSelection(currentPosition.surat - 1)
            ayatEditText!!.setText(currentPosition.ayat.toString())
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
        return Surat.getJumlahAyat(requireContext().applicationContext, surat) >= ayat
    }

    private fun performOkButtonAction() {
        val stringAyat = ayatEditText!!.text.toString()
        val intAyat = Integer.parseInt(stringAyat)
        val intSurat = dropdownSurat!!.selectedItemPosition + 1

        if (isValid(intSurat, intAyat)) {
            if (posisiListener != null) posisiListener!!.SetPosisi(Posisi(intSurat, intAyat))
            dialog?.dismiss()
        } else {
            ayatEditText!!.error = "Ayat yang dipilih tidak ada."
        }
    }

    private val listSurat: Array<String?>
        get() {
            val listNamaSurat = Surat.getListNamaSurat(requireContext().applicationContext)
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
        posisiListener = _listener
    }

    fun setCurrentPosition(posisi: Posisi?) {
        this.posisi = posisi
    }
}
