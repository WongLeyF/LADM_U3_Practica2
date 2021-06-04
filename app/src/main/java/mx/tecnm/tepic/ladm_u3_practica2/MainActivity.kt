package mx.tecnm.tepic.ladm_u3_practica2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {
    var db = FirebaseFirestore.getInstance()
    var dataArray = ArrayList<String>()
    var idArray = ArrayList<String>()
    var pedidos: MutableMap<String, Any> = HashMap()
    var index = 1
    private var removePrecioArray = ArrayList<Float>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonInc.setOnClickListener {
            if (editTextCantidad.text.toString().trim() == "" || editTextCantidad.text.toString()
                    .trim() == "0"
            ) {
                editTextCantidad.error = "Debes agregar minimo un producto!"
                return@setOnClickListener
            } else {
                val cant: Int = editTextCantidad.text.toString().toInt()
                editTextCantidad.setText((cant + 1).toString())
            }

        }

        buttonDec.setOnClickListener {
            if (editTextCantidad.text.toString().trim() == "" || editTextCantidad.text.toString()
                    .trim() == "0"
            ) {
                editTextCantidad.error = "Debes agregar minimo un producto!"
                return@setOnClickListener
            } else {
                val cant: Int = editTextCantidad.text.toString().toInt()
                editTextCantidad.setText(if (cant == 1) "1" else (cant - 1).toString())
            }
        }

        buttonAddProduct.setOnClickListener {
            addProduct()
        }

        buttonAddOrder.setOnClickListener {
            if (editTextProduct.text.toString().trim() != "" || editTextPrecio.text.toString()
                    .trim() != ""
            ) {
                AlertDialog.Builder(this).setTitle("Atencion!")
                    .setMessage("Tienes elementos sin agregar, ¿Deseas continuar?")
                    .setPositiveButton("Cancelar") { _, _ -> }
                    .setNegativeButton("Aceptar") { _, _ -> insertdb() }
                    .show()
            } else insertdb()
        }

        buttonShowOrders.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }

    private fun insertdb() {
        if (requiredEditText(false)) return
        val tempTotal = Pattern.compile("\\$").split(textTotal.text.toString())
        val total: Float = tempTotal[1].toString().toFloat()
        pedidos["total"] = total
        val data = hashMapOf(
            "nombre" to editTextClientName.text.toString(),
            "fecha" to Timestamp.now(),
            "entregado" to false,
            "celular" to editTextPhone.text.toString(),
            "pedido" to pedidos
        )
        db.collection("RESTAURANTE").add(data as Any)
            .addOnSuccessListener {
                mensaje(
                    "Exito!",
                    "Se agrego correctamente el pedido de ${editTextClientName.text}"
                )
                clear(false)
            }
            .addOnFailureListener { e -> mensaje("ERROR", e.message!!) }
    }

    @SuppressLint("SetTextI18n")
    private fun delete(id: String, index: Int) {
        pedidos.remove(id)
        dataArray.removeAt(index)
        val tempTotal = Pattern.compile("\\$").split(textTotal.text.toString())
        var total: Float = tempTotal[1].toString().toFloat()
        total -= removePrecioArray[index]
        textTotal.text = "$${total}0"
        allProducts.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataArray)
    }

    @SuppressLint("SetTextI18n")
    private fun addProduct() {
        if (requiredEditText(true)) return
        val pedido = hashMapOf(
            "cantidad" to editTextCantidad.text.toString().toInt(),
            "descripcion" to editTextProduct.text.toString(),
            "precio" to editTextPrecio.text.toString().toFloat()
        )
        idArray.add("item${index}")
        this.pedidos["item${index++}"] = pedido
        val cad = "\nDescripcion: ${pedido["descripcion"]}" +
                "\nCantidad: ${pedido["cantidad"]}" +
                "\nPrecio: $${pedido["precio"]}"
        dataArray.add(cad)
        val tempTotal = Pattern.compile("\\$").split(textTotal.text.toString())
        var total: Float = tempTotal[1].toString().toFloat()
        val precio: Float = pedido["precio"].toString().toFloat()
        val cantidad: Int = pedido["cantidad"].toString().toInt()
        removePrecioArray.add(precio * cantidad)
        total += precio * cantidad
        textTotal.text = "$${total}0"
        if (dataArray.isEmpty()) dataArray.add("No se encontró ningún apartado")
        allProducts.adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataArray)
        this.registerForContextMenu(allProducts)
        allProducts.setOnItemClickListener { _, _, i, _ ->
            dialogDelUpt(i)
        }
        clear(true)
    }

    private fun mensaje(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { d, _ -> d.dismiss() }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun clear(section: Boolean) {
        if (section) {
            editTextPrecio.setText("")
            editTextProduct.setText("")
            editTextCantidad.setText("1")
        } else {
            editTextClientName.setText("")
            editTextPhone.setText("")
            textTotal.text = "$0.00"
            dataArray.clear()
            idArray.clear()
            pedidos.clear()
            index = 1
            allProducts.adapter =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataArray)
        }
    }

    private fun dialogDelUpt(index: Int) {
        val id = this.idArray[index]
        AlertDialog.Builder(this).setTitle("Atencion!")
            .setMessage("¿Que desea hacer con ${id}\n ${dataArray[index]}?")
            .setPositiveButton("Cancelar") { _, _ -> }
            .setNegativeButton("Eliminar") { _, _ -> delete(id, index) }
            .show()
    }

    private fun requiredEditText(section: Boolean): Boolean {
        if (section) {
            if (editTextCantidad.text.toString().trim() == "" || editTextCantidad.text.toString()
                    .trim() == "0"
            ) {
                editTextCantidad.error = "Debes agregar minimo un producto!"
                return true
            }
            if (editTextProduct.text.toString().trim() == "") {
                editTextProduct.error = "El producto es requerido!"
                editTextProduct.hint = "Ingresa el nombre del producto"
                return true

            }
            if (editTextPrecio.text.toString().trim() == "") {
                editTextPrecio.error = "El precio es requerido!"
                editTextPrecio.hint = "Ingresa el precio"
                return true
            }
        } else {
            if (editTextClientName.text.toString().trim() == "") {
                editTextClientName.error = "El nombre del cliente es requerido!"
                editTextClientName.hint = "Ingresa el nombre del cliente"
                return true
            }
            if (editTextPhone.text.toString().trim() == "") {
                editTextPhone.error = "El telefono de cliente es requerido!"
                editTextPhone.hint = "Ingresa el telefono del cliente"
                return true

            }
        }
        return false
    }

}