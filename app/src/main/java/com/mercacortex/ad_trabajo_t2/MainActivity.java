package com.mercacortex.ad_trabajo_t2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.mercacortex.ad_trabajo_t2.utils.Memoria;
import com.mercacortex.ad_trabajo_t2.utils.RestClient;
import com.mercacortex.ad_trabajo_t2.utils.Resultado;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 *  Crear una aplicación que permita mostrar una secuencia de imágenes y frases en pantalla.
 *  Se introducirán las rutas al fichero con enlaces a imágenes en la red y
 *  al fichero con frases para poder realizar su descarga.
 *  Por defecto, los ficheros de imágenes y frases estarán situados en alumno.mobi/~alumno/superior/primerapellido/
 *  (cada alumno preparará los ficheros necesarios en su carpeta).
 *
 *  El fichero de imágenes contendrá en cada línea un enlace a una imagen y
 *  el de frases tendrá en cada línea una frase célebre. Pueden ser diferentes el número de imágenes y el de frases.
 *  ejemplo de fichero imagenes.txt:
 *      https://i.imgur.com/tGbaZCY.jpg
 *      http://192.168.2.50/imagen/foto.jpg
 *      http://192.168.2.50/noexiste.png
 *      https://i.imgur.com/MU2dD8E.jpg
 *
 *  ejemplo de fichero frases.txt:
 *      La mayoría de los sueños no se viven, se roncan (Poncela)
 *      Que hablen de uno es espantoso, pero hay algo peor: que no hablen (Wilde)
 *      La vida es aquello que te va sucediendo mientras te empeñas en hacer otros planes (Lennon)
 *      Vive como si fueras a morir mañana. Aprende como si fueras a vivir siempre (Gandhi)
 *      No esperes a ser valiente para actuar, actúa como si ya fueras valiente (Alfonso Alcántara)
 *
 *  Cuando se pulse el botón de descarga, se mostrarán una a una las imágenes y frases descargadas,
 *  de forma automática cada cierto tiempo. Ese tiempo (en segundos) estará almacenado en el fichero /raw/intervalo.txt.
 *
 *  Además, se añadirán al fichero errores.txt situado en un servidor web
 *  (en Internet:  alumno.mobi/~alumno/superior/primerapellido/)
 *  los errores que se hayan producido:
 *      no se puede descargar el fichero de imágenes o el de frases
 *      no se puede descargar alguna imagen, etc.
 *
 *  Por cada error producido, se añadirá al fichero errores.txt una
 *  línea con la ruta al archivo que se quiere descargar,
 *  la fecha y hora de acceso y la causa del error
 *  (fallo en el servidor web, no existe el fichero, . . . ).
 */
public class MainActivity extends AppCompatActivity {

    private static final String FICHERO_FRASES = "frases.txt";
    private static final String FICHERO_IMAGENES = "imagenes.txt";
    private static final String FICHERO_INTERVALO = "intervalo";
    private static final String FICHERO_ERROR = "errores.txt";
    private static final long DURACION = 120000; //120 segundos
    private static final String WEB = "http://192.168.0.139/acceso/php/upload.php";
    private static final String WEB_ERROR = "http://alumno.mobi/diaz/errores.txt";
    private static final String PASSWORD = "123";
    private EditText edtImagenes;
    private EditText edtFrases;
    private TextView txvFrases;
    private ImageView imvImagen;
    private Memoria memoria;
    private ProgressDialog progress;
    private String frases, imagenes;
    private boolean frasesDescargadas = false;
    private boolean imagenesDescargadas = false;
    private long intervalo = 5000L;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memoria = new Memoria(this);
        progress = new ProgressDialog(this);

        edtFrases = findViewById(R.id.edtFrases);
        edtImagenes = findViewById(R.id.edtImagenes);
        imvImagen = findViewById(R.id.imvImagen);
        txvFrases = findViewById(R.id.txvFrases);

        Resultado resultado = memoria.leerRaw(FICHERO_INTERVALO);
        if (resultado.getCodigo())
            intervalo = Long.parseLong(resultado.getContenido());
    }

    /**
     * Se descarga el fichero
     * @param view Botón
     */
    public void onClick(View view) {
        //Primero tendría que ser la descarga de uno y luego la del otro
        download(edtFrases.getText().toString(), FICHERO_FRASES);
        download(edtImagenes.getText().toString(), FICHERO_IMAGENES);
    }

    /**
     * Crea un temporizador de 2 minutos que cambia de imagen y frase según
     * el intervalo indicado en el archivo intervalos.txt
     */
    private void onDownLoadFinished() {
        //Comprueba que se hayan realizado las descargas antes de mostrar nada
        if(frasesDescargadas && imagenesDescargadas)
            timer = new CountDownTimer(DURACION, intervalo) {
                String[] frasesArchivo = frases.split("\n");
                String[] rutasImagenes = imagenes.split("\n");
                int contador = 0;

                public void onTick(long millisUntilFinished) {
                    contador++;
                    cambiaImagenFrase(
                            frasesArchivo[contador % frasesArchivo.length],
                            rutasImagenes[contador % rutasImagenes.length]
                    );
                }
                public void onFinish() {
                    Toast.makeText(getApplicationContext(), "Mostradas todas las imágenes. ¡2 veces!", Toast.LENGTH_LONG).show();
                }
            }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    /**
     * Descarga desde la ruta al fichero local indicados
     * @param origin Ruta origen del archivo de descarga
     * @param destination Ruta destino del archivo descargado
     */
    private void download(String origin, String destination) {
        if (!origin.startsWith("http://") && !origin.startsWith("https://"))
            origin = "http://" + origin;
        new FileAsynTask().execute(origin, destination);
        //Hago operaciones pertinentes según la ruta
        switch (destination) {
            case FICHERO_FRASES:
                break;
            case FICHERO_IMAGENES:
                break;
        }
    }

    private void cambiaImagenFrase(String frase, final String miRuta) {
        txvFrases.setText(frase);
        new RestClient().get(miRuta, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Picasso.with(MainActivity.this)
                            .load(miRuta)
                            .error(R.drawable.error)
                            .placeholder(R.drawable.placeholder)
                            .into(imvImagen);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    mostrarMensaje("Error: fallo al descargar la imagen");
                }
            });
    }

    /**
     * Método que escribe en memoria un fichero
     *
     * @param file Fichero que escribir
     * @param path Ruta en la memoria externa
     */
    private void writeInMemory(File file, String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            content.append(line).append("\n");
        reader.close();
        if (memoria.disponibleEscritura()) {
            memoria.escribirExterna(path, content.toString(), false, Memoria.UTF8);
        } else
            mostrarMensaje("Error: no se escribió el archivo");
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clase que inicia la descarga de los ficheros necesarios
     */
    class FileAsynTask extends AsyncTask<String, Void, String> {
        File downloadFile;
        private RestClient restClient;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setMessage("Conectando . . .");
            restClient = new RestClient();
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    restClient.cancelRequests(MainActivity.this, true);
                }
            });
            progress.show();
        }

        @Override
        protected String doInBackground(String... args) {
            restClient.get(args[0], new FileAsyncHttpResponseHandler(MainActivity.this) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    cancel(true);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File response) {
                    downloadFile = response;
                }
            });
            return args[1];
        }

        @Override
        protected void onPostExecute(String destination) {
            super.onPostExecute(destination);
            progress.dismiss();
            try {
                writeInMemory(downloadFile, destination);
            } catch (IOException e) {
                mostrarMensaje("Error: fallo en la descarga");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progress.dismiss();
        }

    }
}
