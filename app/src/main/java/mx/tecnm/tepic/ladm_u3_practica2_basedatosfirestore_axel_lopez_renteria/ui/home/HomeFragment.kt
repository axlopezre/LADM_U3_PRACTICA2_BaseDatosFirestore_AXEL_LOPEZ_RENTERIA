package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.ui.home

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var arreglo = ArrayList<String>()
    var listaID = ArrayList<String>()
    val baseRemota = FirebaseFirestore.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //CODIGO
        AlertDialog.Builder(requireContext())
            .setTitle("VENTANA INFORMATIVA")
            .setMessage("PARA CONSULTAR SE TIENE QUE AGREGAR LA INFORMACION QUE REQUIERE CON SU RESPECTIVO CUADRO DE TEXTO Y POR CONSIGUIENTE PRESIONAR EL BOTON DE CONSULTAR\n" +
                    "\nCONDICION: NO SE PUEDE ELIMINAR UN AUTO QUE ESTÉ ARRENDADO"+
                    "\nPARA CONSULTAR UN RANGO DE KILOMETRAJE EL FORMATO ES: (MÍNIMO,MÁXIMO)\n" +
                    "\nPARA ELIMINAR Y ACTUALIZAR ES PRESIONAR EL AUTOMOVIL(EN EL LISTVIEW) Y TE DARÁ LAS OPCIONES AUTOMATICAMENTE")
            .show()
        FirebaseFirestore.getInstance()
            .collection("AUTOMOVIL")
            .addSnapshotListener { query, error ->
                if (error != null) {
                    //SI HUBO ERROR!
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                arreglo.clear()
                listaID.clear()
                for (documento in query!!) {
                    var cadena =
                        "MODELO: ${documento.getString("MODELO")}\nMARCA: ${documento.getString("MARCA")}\nKILOMETRAJE: ${
                            documento.getLong("KILOMETRAGE")
                        }"
                    arreglo.add(cadena)
                    listaID.add(documento.id.toString())//obtener el id de cada documento
                }
                binding.lista.adapter = ArrayAdapter<String>(
                    requireContext(),
                    R.layout.simple_list_item_1, arreglo
                )
                binding.lista.setOnItemClickListener { adapterView, view, posicion, l ->
                    dialogoEliminaActualiza(posicion)
                }
            }
        binding.insertar.setOnClickListener {
            val baseRemota = FirebaseFirestore.getInstance()

            val datos = hashMapOf(
                "MARCA" to binding.marca.text.toString(),
                "MODELO" to binding.modelo.text.toString(),
                "KILOMETRAGE" to binding.kilometrage.text.toString().toInt()
            )

            baseRemota.collection("AUTOMOVIL")
                .add(datos)

                .addOnSuccessListener {
                    // SI SE PUDO!
                    Toast.makeText(requireContext(),"EXITO! SE INSERTO CORRECTAMENTE EN LA NUBE", Toast.LENGTH_LONG)
                }
                .addOnFailureListener{
                    //NO SE PUDO!
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.message)
                        .show()
                }
            binding.marca.setText("")
            binding.modelo.setText("")
            binding.kilometrage.setText("")
        }

        binding.consultar.setOnClickListener {

            var text=""
            var consulta= baseRemota.collection("AUTOMOVIL").whereEqualTo("MARCA", binding.marca.text.toString())
            if(!binding.marca.text.toString().equals("")) {
                consulta = baseRemota.collection("AUTOMOVIL").whereEqualTo("MARCA", binding.marca.text.toString())
            }else if(!binding.modelo.text.toString().equals("")) {
                consulta = baseRemota.collection("AUTOMOVIL").whereEqualTo("MODELO", binding.modelo.text.toString())
            }else if(!binding.kilometrage.text.toString().equals("")) {
                var kil = binding.kilometrage.text.toString()
                val dividir = kil.split(",")
                consulta = baseRemota.collection("AUTOMOVIL").whereGreaterThanOrEqualTo("KILOMETRAGE", dividir[0].toInt()).whereLessThanOrEqualTo("KILOMETRAGE", dividir[1].toInt())
            }
            consulta.get()
                .addOnSuccessListener {
                    for (documento in it) {
                         text += "MODELO: ${documento.getString("MODELO")} \nMARCA: ${documento.getString("MARCA")} \nKILOMETRAGE: ${
                                documento.getLong("KILOMETRAGE")}"
                    }
                    if(text == ""){
                        AlertDialog.Builder(requireContext())
                            .setTitle("ERROR")
                            .setMessage("No existen los datos que buscas")
                            .setNeutralButton("CERRAR"){d,i->}
                            .show()
                        return@addOnSuccessListener
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle("EXITO, SE RECUPERARON LOS DATOS")
                        .setMessage(text)
                        .setNeutralButton("CERRAR"){d,i->}
                        .show()
                }
                .addOnFailureListener {
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.message)
                        .show()
                }
        }

        return root
    }
    private fun dialogoEliminaActualiza(posicion: Int) {
        var idElegido = listaID.get(posicion)
        AlertDialog.Builder(requireContext()).setTitle("ATENCIÓN")
            .setMessage("QUE DESEAS HACER CON \n${arreglo.get(posicion)}?")
            .setNeutralButton("ELIMINAR"){d,i->
                 eliminar(idElegido)
            }
            .setPositiveButton("ACTUALIZAR"){d,i->
                 actualizar(idElegido)
            }
            .setNegativeButton("ACEPTAR"){d, i->}
            .show()
    }

    private fun eliminar(idElegido: String) {
        val baseRemota = FirebaseFirestore.getInstance()
        var bandera = false
        baseRemota.collection("ARRENDAMIENTO").whereEqualTo("IDAUTO", idElegido).get()
            .addOnSuccessListener {
                for (documento in it){
                    bandera = true
                }
                if(bandera){
                    AlertDialog.Builder(requireContext())
                        .setTitle("ERROR")
                        .setMessage("NO SE PUEDE BORRAR ESTE AUTOMOVIL, UN ARRENDAMIENTO LO ESTÁ UTILIZANDO!")
                        .setNeutralButton("Cerrar") { d, i -> }
                        .show()
                    return@addOnSuccessListener
                }else{
                    baseRemota.collection("AUTOMOVIL")
                        .document(idElegido)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "SE HA ELIMINADO EL AUTOMOVIL", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            AlertDialog.Builder(requireContext())
                                .setMessage(it.message)
                                .show()
                        }
                }
            }
            .addOnFailureListener {
                AlertDialog.Builder(requireContext())
                    .setMessage(it.message)
                    .show()
            }
    }

    private fun actualizar(idElegido: String) {
        var otraVentana = Intent(requireContext(), MainActivity2::class.java)
        otraVentana.putExtra("idelegido", idElegido)
        startActivity(otraVentana)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}