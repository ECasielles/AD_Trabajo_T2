package com.mercacortex.ad_trabajo_t2.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Memoria {
    private static final int FILE_PICKER_REQUEST_CODE = 1;
    public static final String UTF8 = "UTF-8";
    private Context context;

    public Memoria(Context context) {
        this.context = context;
    }

    public boolean escribirInterna(String fichero, String cadena, Boolean anadir, String codigo) {
        FileOutputStream stream = null;
        boolean resultado = false;
        try {
            if(anadir)
                stream = context.openFileOutput(fichero, Context.MODE_PRIVATE | Context.MODE_APPEND);
            else
                stream = context.openFileOutput(fichero, Context.MODE_PRIVATE);
            return escribir(stream, cadena, codigo);
        } catch (FileNotFoundException e) {
            Log.e("Archivo inexistente: ", e.getMessage());
        } finally {
            if(stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("Error al cerrar", e.getMessage());
                }
        }
        return resultado;
    }

    private boolean escribir(FileOutputStream stream, String cadena, String codigo) {
        BufferedWriter out = null;
        boolean correcto = false;
        try {
            out = new BufferedWriter(new OutputStreamWriter(stream, codigo));
            out.write(cadena);
        } catch (IOException e) {
            Log.e("Error de E/S", e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                    correcto = true;
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
            }
        }
        return correcto;
    }

    public String mostrarPropiedadesInterna(String fichero) {
        File miFichero;
        //miFichero = new File(contexto.getFilesDir(), fichero);
        miFichero = new File(fichero);
        return mostrarPropiedades(miFichero);
    }

    public String mostrarPropiedades(File fichero) {
        SimpleDateFormat formato = null;
        StringBuffer txt = new StringBuffer();
        try {
            if (fichero.exists()) {
                txt.append("Nombre: " + fichero.getName() + '\n');
                txt.append("Ruta: " + fichero.getAbsolutePath() + '\n');
                txt.append("TamaÃ±o (bytes): " + Long.toString(fichero.length()) + '\n');
                formato = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
                txt.append("Fecha: " + formato.format(new Date(fichero.lastModified())) + '\n');
            } else
                txt.append("No existe el fichero " + fichero.getName() + '\n');
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            txt.append(e.getMessage());
        }
        return txt.toString();
    }

    public boolean disponibleEscritura() {

        boolean escritura = false;

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();

        if (estado.equals(Environment.MEDIA_MOUNTED))
            escritura = true;
        return escritura;
    }

    public boolean disponibleLectura() {
        boolean lectura = false;

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        String estado = Environment.getExternalStorageState();
        if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || estado.equals(Environment.MEDIA_MOUNTED))
            lectura = true;
        return lectura;
    }

    public boolean escribirExterna(String fichero, String cadena, Boolean anadir, String codigo) {
        File miFichero, tarjeta;
        tarjeta = Environment.getExternalStorageDirectory();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        FileOutputStream stream = null;
        boolean resultado = false;
        try {
            resultado = escribir(new FileOutputStream(miFichero, anadir), cadena, codigo);
        } catch (FileNotFoundException e) {
            Log.e("Archivo inexistente: ", e.getMessage());
        } finally {
            if(stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("Error al cerrar", e.getMessage());
                }
        }
        return resultado;
    }

    public String mostrarPropiedadesExterna(String fichero) {
        File miFichero, tarjeta;
        tarjeta = Environment.getExternalStorageDirectory();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        return mostrarPropiedades(miFichero);
    }

    public Resultado leerInterna(String fichero, String codigo) {
        FileInputStream stream = null;
        Resultado resultado = new Resultado();
        resultado.setCodigo(false);
        try {
            stream = context.openFileInput(fichero);
            resultado = leer(stream, codigo);
        } catch (FileNotFoundException e) {
            Log.e("Archivo inexistente: ", e.getMessage());
        } finally {
            if(stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("Error al cerrar", e.getMessage());
                }
        }
        return resultado;
    }

    private Resultado leer(FileInputStream stream, String codigo) {
        BufferedReader reader = null;
        StringBuilder miCadena = new StringBuilder();
        Resultado resultado = new Resultado();
        resultado.setCodigo(true);
        try {
            reader = new BufferedReader(new InputStreamReader(stream, codigo));
            int n;
            while ((n = reader.read()) != -1)
                miCadena.append((char) n);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }

    public Resultado leerExterna(String fichero, String codigo) {
        File miFichero, tarjeta;
        tarjeta = Environment.getExternalStorageDirectory();
        miFichero = new File(tarjeta.getAbsolutePath(), fichero);
        FileInputStream stream = null;
        Resultado resultado = new Resultado();
        resultado.setCodigo(false);
        try {
            resultado = leer(new FileInputStream(miFichero), codigo);
        } catch (FileNotFoundException e) {
            Log.e("Archivo inexistente: ", e.getMessage());
        } finally {
            if(stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("Error al cerrar", e.getMessage());
                }
        }
        return resultado;
    }

    public Resultado leerRaw(String fichero){
        //fichero tendrá el nombre del fichero raw sin la extensión
        InputStream is = null;
        StringBuilder miCadena = new StringBuilder();
        int n;
        Resultado resultado = new Resultado();
        resultado.setCodigo(true);
        try {
            is = context.getResources().openRawResource(
                    context.getResources().getIdentifier(fichero,"raw", context.getPackageName()));
            while ((n = is.read()) != -1) {
                miCadena.append((char) n);
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            resultado.setCodigo(false);
            resultado.setMensaje(e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                    resultado.setContenido(miCadena.toString());
                }
            } catch (Exception e) {
                Log.e("Error al cerrar", e.getMessage());
                resultado.setCodigo(false);
                resultado.setMensaje(e.getMessage());
            }
        }
        return resultado;
    }

}