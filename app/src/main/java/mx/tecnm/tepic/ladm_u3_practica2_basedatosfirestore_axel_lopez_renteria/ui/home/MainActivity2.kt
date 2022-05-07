package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {
    var idElegido = ""
    lateinit var binding: ActivityMain2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        idElegido = intent.extras!!.getString("idelegido")!!
        val baseRemota = FirebaseFirestore.getInstance()
        baseRemota.collection("AUTOMOVIL")
            .document(idElegido)
            .get()//Obtiene un documento
            .addOnSuccessListener {
                binding.marca.setText(it.getString("MARCA"))
                binding.modelo.setText(it.getString("MODELO"))
                binding.kilometrage.setText(it.getLong("KILOMETRAGE").toString())
            }
            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setMessage(it.message)
                    .show()
            }
        binding.regresar.setOnClickListener {
            finish()
        }
        binding.actualizar.setOnClickListener {
            val baseRemota = FirebaseFirestore.getInstance()
            baseRemota.collection("AUTOMOVIL")
                .document(idElegido)
                .update("MARCA", binding.marca.text.toString(),
                    "MODELO", binding.modelo.text.toString(),
                    "KILOMETRAGE",binding.kilometrage.text.toString().toInt())
                .addOnSuccessListener {
                    Toast.makeText(this,"EXITO! SE ACTUALIZO CORRECTAMENTE EN LA NUBE", Toast.LENGTH_LONG).show()
                    binding.modelo.setText("")
                    binding.marca.setText("")
                    binding.kilometrage.text.clear()
                }
                .addOnFailureListener {
                    AlertDialog.Builder(this)
                        .setMessage(it.message)
                        .show()
                }
        }
    }
}