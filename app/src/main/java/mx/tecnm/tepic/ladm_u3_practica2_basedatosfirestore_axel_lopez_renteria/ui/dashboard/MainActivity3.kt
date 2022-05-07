package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.databinding.ActivityMain3Binding

class MainActivity3 : AppCompatActivity() {
    var idElegido=""
    lateinit var binding: ActivityMain3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        idElegido = intent.extras!!.getString("idelegido")!!
        val baseRemota = FirebaseFirestore.getInstance()
        baseRemota.collection("ARRENDAMIENTO")
            .document(idElegido)
            .get()
            .addOnSuccessListener {
                binding.nombre.setText(it.getString("NOMBRE"))
                binding.domicilio.setText(it.getString("DOMICILIO"))
                binding.licencia.setText(it.getString("LICENCIACOND"))
                binding.modelo.setText(it.getString("MODELO"))
                binding.marca.setText(it.getString("MARCA"))
                binding.fecha.setText(it.getDate("FECHA").toString())
            }
            .addOnFailureListener {
                AlertDialog.Builder(this)
                    .setMessage(it.message)
                    .show()
            }
        binding.fecha.isEnabled = false
        binding.regresar.setOnClickListener {
            finish()
        }

        binding.actualizar.setOnClickListener{
            val baseRemota= FirebaseFirestore.getInstance()
            val baseRemota2= FirebaseFirestore.getInstance()
            baseRemota2.collection("AUTOMOVIL")
                .whereEqualTo("MARCA", binding.marca.text.toString())
                .whereEqualTo("MODELO", binding.modelo.text.toString())
                .get()
                .addOnSuccessListener {
                    var bandera = false
                    for (documento in it){
                        bandera = true
                    }
                    if (!bandera){
                        AlertDialog.Builder(this)
                            .setTitle("ERROR")
                            .setMessage("INGRESA UN AUTOMOVIL EXISTENTE")
                            .setNeutralButton("Cerrar"){d,i->}
                            .show()
                        return@addOnSuccessListener
                    }
                    baseRemota.collection("ARRENDAMIENTO")
                        .document(idElegido)
                        .update("NOMBRE", binding.nombre.text.toString(),
                            "DOMICILIO", binding.domicilio.text.toString(),
                            "LICENCIACOND", binding.licencia.text.toString(),
                            "MODELO", binding.modelo.text.toString(),
                            "IDAUTO", it.documents[0].id,
                            "MARCA", binding.marca.text.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "SE ACTUALIZÓ CON ÉXITO", Toast.LENGTH_LONG).show()
                            binding.marca.text.clear()
                            binding.modelo.text.clear()
                            binding.nombre.text.clear()
                            binding.domicilio.text.clear()
                            binding.licencia.text.clear()
                        }
                        .addOnFailureListener {
                            AlertDialog.Builder(this)
                                .setMessage(it.message)
                                .show()
                        }

                }
                .addOnFailureListener {
                    AlertDialog.Builder(this)
                        .setMessage(it.message)
                        .show()
                }
        }
    }
}