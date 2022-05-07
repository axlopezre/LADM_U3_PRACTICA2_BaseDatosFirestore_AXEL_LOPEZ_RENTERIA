package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.ui.dashboard

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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.databinding.FragmentDashboardBinding
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_axel_lopez_renteria.ui.home.MainActivity2

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    var arreglo = ArrayList<String>()
    var listaID = ArrayList<String>()
    var text = ""
    val baseRemota = FirebaseFirestore.getInstance()
    val baseRemota2 = FirebaseFirestore.getInstance()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //CODIGO
        AlertDialog.Builder(requireContext())
            .setTitle("VENTANA INFORMATIVA")
            .setMessage("PARA CONSULTAR SE TIENE QUE AGREGAR LA INFORMACION QUE REQUIERE CON SU RESPECTIVO CUADRO DE TEXTO Y POR CONSIGUIENTE PRESIONAR EL BOTON DE CONSULTAR\n" +
                    "\nCONDICION: PARA AGREGAR UN ARRENDAMIENTO ES NECESARIO QUE EL AUTOMOVIL EXISTA(OBVIAMENTE SI NO EXISTE NO PERMITIRÁ)"+
                    "\nPARA ELIMINAR Y ACTUALIZAR ES PRESIONAR EL ARRENDAMIENTO(EN EL LISTVIEW) Y TE DARÁ LAS OPCIONES AUTOMATICAMENTE")
            .show()
        FirebaseFirestore.getInstance()
            .collection("ARRENDAMIENTO")
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
                        "NOMBRE: ${documento.getString("NOMBRE")}\nDOMICILIO: ${documento.getString("DOMICILIO")}\nLICENCIA: ${
                            documento.getString("LICENCIACOND")}\nMODELO: ${documento.getString("MODELO")}\nMARCA: ${documento.getString("MARCA")}" +
                                "\nIDAUTO: ${documento.getString("IDAUTO")}\nFECHA: ${documento.getDate("FECHA")}\n______<______>_______<_______>____<____>___"
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
            baseRemota2.collection("AUTOMOVIL")
                .whereEqualTo("MARCA", binding.marca.text.toString())
                .whereEqualTo("MODELO", binding.modelo.text.toString())
                .get()
                .addOnSuccessListener {
                    for (documento in it){
                        val datos = hashMapOf(
                            "NOMBRE" to binding.nombre.text.toString(),
                            "DOMICILIO" to binding.domicilio.text.toString(),
                            "LICENCIACOND" to binding.licencia.text.toString(),
                            "MODELO" to binding.modelo.text.toString(),
                            "MARCA" to binding.marca.text.toString(),
                            "IDAUTO" to documento.id,
                            "FECHA" to Timestamp.now()
                        )
                        baseRemota.collection("ARRENDAMIENTO").add(datos)
                            .addOnSuccessListener{
                                Toast.makeText(requireContext(), "SE INSERTÓ CORRECTAMENTE", Toast.LENGTH_LONG).show()
                                binding.nombre.setText("")
                                binding.domicilio.setText("")
                                binding.licencia.setText("")
                                binding.modelo.setText("")
                                binding.marca.setText("")
                            }
                            .addOnFailureListener {
                                // NO SE PUDO
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
        binding.consultar.setOnClickListener {
            var text=""
            var consulta= baseRemota.collection("ARRENDAMIENTO").whereEqualTo("NOMBRE", binding.nombre.text.toString())
            if(!binding.nombre.text.toString().equals("")) {
                consulta = baseRemota.collection("ARRENDAMIENTO").whereEqualTo("NOMBRE", binding.nombre.text.toString())
            }else if(!binding.domicilio.text.toString().equals("")) {
                consulta = baseRemota.collection("ARRENDAMIENTO").whereEqualTo("DOMICILIO", binding.domicilio.text.toString())
            }else if(!binding.licencia.text.toString().equals("")) {
                consulta = baseRemota.collection("ARRENDAMIENTO").whereGreaterThanOrEqualTo("LICENCIACOND", binding.licencia.text.toString())
            }else if(!binding.modelo.text.toString().equals("")) {
                consulta = baseRemota.collection("ARRENDAMIENTO").whereGreaterThanOrEqualTo("MODELO", binding.modelo.text.toString())
            }else if(!binding.marca.text.toString().equals("")) {
                consulta = baseRemota.collection("ARRENDAMIENTO").whereGreaterThanOrEqualTo("MARCA", binding.marca.text.toString())
            }
            consulta.get()
                .addOnSuccessListener {
                    for (documento in it) {
                        text += "\nNOMBRE: ${documento.getString("NOMBRE")} \nDOMICILIO: ${documento.getString("DOMICILIO")} \nLICENCIA: ${
                            documento.getString("LICENCIACOND")}\nMODELO: ${documento.getString("MODELO")}\nMARCA: ${documento.getString("MARCA")}"
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
        val baseRemota=FirebaseFirestore.getInstance()
        baseRemota.collection("ARRENDAMIENTO")
            .document(idElegido)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "SE ELIMINÓ EL ARRENDAMIENTO CORRECTAMENTE", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                AlertDialog.Builder(requireContext())
                    .setMessage(it.message)
                    .show()
            }
    }
    private fun actualizar(idElegido: String){
        var otraVentana = Intent(requireContext(), MainActivity3::class.java)
        otraVentana.putExtra("idelegido", idElegido)
        startActivity(otraVentana)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}