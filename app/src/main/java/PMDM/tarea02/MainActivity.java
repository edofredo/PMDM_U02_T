package PMDM.tarea02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button conexion;
    Button enviarSms;
    EditText mensaje;
    EditText telefono;
    String infoRed;
    WebView webView;
    TextView datosAlumno;


    //ID de peticion para identificar la peticion de ese permiso (me invento este id)
    static final int ID_PETICION_PERMISO_SEND_SMS=1111;

    //inicializa la activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conexion = (Button) findViewById(R.id.butConexion);
        conexion.setOnClickListener(this);

        enviarSms = (Button) findViewById(R.id.butEnviar);
        enviarSms.setOnClickListener(this);

        mensaje = (EditText) findViewById(R.id.etMensaje);
        telefono = (EditText) findViewById(R.id.etTelefono);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://es.wikipedia.org/");

        setSharedPreferences();
        getSharedPreferences();
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.butConexion){
            comprobarConexion(view);
            String text = infoRed;
            int duracion = Toast.LENGTH_SHORT;
            Toast.makeText(getApplicationContext(),text,duracion).show();

        }

        if(view.getId() == R.id.butEnviar){

            if(!telefono.getText().toString().isEmpty() && !mensaje.getText().toString().isEmpty()) {
                sendSMSByCode(telefono.getText().toString(), mensaje.getText().toString());

            }
            else{
                Toast.makeText(getApplicationContext(), "Ha dejado algun campo vacio",Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void comprobarConexion(View v) {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            Network[] redes = cm.getAllNetworks();
            if(redes.length==0){
                infoRed = "No hay redes disponibles";
            }
            for(Network red:redes){
                NetworkInfo redInfo = cm.getNetworkInfo(red);
                infoRed = getStrNetInfo(redInfo);
            }
        } else {
            NetworkInfo [] redesInfo = cm.getAllNetworkInfo();
            for(NetworkInfo redInfo : redesInfo){
                infoRed = getStrNetInfo(redInfo);
            }
        }

    }

    private String getStrNetInfo(NetworkInfo redInfo) {
        String strMessage = "Red " + redInfo.getTypeName();
        if(redInfo.isAvailable()){
            strMessage+=" esta disponible";
            if(redInfo.isConnected()){
                strMessage+=" y conectada";
            } else {
                strMessage+=" pero no conectada";
            }
        }
        return strMessage;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permission, int [] grantResult) {

        super.onRequestPermissionsResult(requestCode, permission, grantResult);

        //Donde comprobamos que peticion es
        switch (requestCode){
            case ID_PETICION_PERMISO_SEND_SMS:
                //Si es la respuesta a la la peticion que hicimos
                // comprobamos si la respuesta es positiva o negativa
                if(grantResult[0]== PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permiso concedido",Toast.LENGTH_LONG).show();
                    //Si es true enviamos el sms
                    sendSMSByCode(telefono.getText().toString(),mensaje.getText().toString());

                }
                else{
                    Toast.makeText(this, "Permiso NO concedido",Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    //Envia un sms por medio de SMS manager
    public void sendSMSByCode(String number, String text){

        //1.- Chequear si tiene el permiso necesario o no
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
        {
            SmsManager smsManager = SmsManager.getDefault();

            //Si el mensaje es mayor de 140 caracteres lo dividimos y los enviamos por trozos
            if(text.length()>140) {
                ArrayList<String> list = smsManager.divideMessage(text);
                for(String fragmento_texto : list) {
                    smsManager.sendTextMessage(number, null, fragmento_texto, null, null);
                    Toast.makeText(getApplicationContext(), "Se han enviado los mensaje",Toast.LENGTH_SHORT).show();

                }
            }
            else{ //Envia el mensaje simple
                smsManager.sendTextMessage(number, null, text, null, null);
                Toast.makeText(getApplicationContext(), "Se ha enviado el mensaje",Toast.LENGTH_SHORT).show();
                ;
            }
        }
        else{
            //2.- Si no tenemos el permiso hacemos una petición
            // (contexto, array de permisos y el codigo de pèticion)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    ID_PETICION_PERMISO_SEND_SMS);

        }
    }

    public void setSharedPreferences(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString("Nombre","Cristian");
        edit.putString("Apellidos","Ade Medina");
        edit.putString("DNI","54212045X");
        edit.commit();
    }

    public void getSharedPreferences(){
        SharedPreferences sharedPref = getSharedPreferences("MainActivity",Context.MODE_PRIVATE);
        String nombre = sharedPref.getString("Nombre", "");
        String apellidos = sharedPref.getString("Apellidos", "");
        String dni = sharedPref.getString("DNI", "");
        String datos = nombre +" "+ apellidos +" "+ dni;
        datosAlumno=(TextView) findViewById(R.id.tvNombreAlumno);
        datosAlumno.setText(datos);
    }


}