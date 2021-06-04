package mx.tecnm.tepic.ladm_u3_practica2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var db = FirebaseFirestore.getInstance()
    var dataArray = ArrayList<String>()
    var infoArray = ArrayList<String>()
    var idArray = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        getFirestore()
    }

    private fun getFirestore() {
        try {
            db.collection("RESTAURANTE").addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    mensaje("ERROR", e.message!!)
                    return@addSnapshotListener
                }
                dataArray.clear()
                idArray.clear()
                for (doc in querySnapshot!!) {
                    if (doc.getBoolean("entregado") == false) {
                        var cad = "Nombre: ${doc.getString("nombre")}" +
                                "\nFecha: ${doc.getDate("fecha")}" +
                                "\nCelular: ${doc.getString("celular")}" +
                                "\nTotal: ${doc.get("pedido.total")}"
                        dataArray.add(cad)
                        val data = doc.get("pedido") as Map<*, *>
                        cad = ""
                        for (i in 1 until data.size) {
                            var temp = data["item${i}"].toString()
                            temp = removeChars(temp, ",", "\n")
                            temp = removeChars(temp, "=", ": ")
                            cad += ("\nItem${i}:\n" + " ${removeChars(temp, "{}")}" + "\n")
                        }
                        infoArray.add(cad)
                        idArray.add(doc.id)
                    }
                }
                if (dataArray.isEmpty()) dataArray.add("No se encontró ningún apartado")
                resultFirestore.adapter =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataArray)
                this.registerForContextMenu(resultFirestore)
                resultFirestore.setOnItemClickListener { _, _, i, _ ->
                    dialogUpt(i)
                }
            }
        } catch (e: Exception) {
            mensaje("ERROR", e.message!!)
        }
    }

    fun removeChars(s: String, c: String, r: String = "") = s.replace(Regex("[$c]"), r)

    private fun mensaje(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { d, _ -> d.dismiss() }
            .show()
    }

    private fun dialogUpt(index: Int) {
        val id = this.idArray[index]
        AlertDialog.Builder(this).setTitle("Atencion!")
            .setMessage("¿Que desea hacer con \n${dataArray[index]}?")
            .setPositiveButton("Cancelar") { _, _ -> }
            .setNeutralButton("Info") { _, _ ->
                mensaje(
                    "Informacion extra del Pedido",
                    infoArray[index]
                )
            }
            .setNegativeButton("Entregar") { _, _ ->
                db.collection("RESTAURANTE").document(id).update("entregado", true)
                Toast.makeText(this, "Se entrego el pedido", Toast.LENGTH_LONG).show()

            }
            .show()
    }

}