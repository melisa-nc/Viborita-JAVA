package com.melisanc.a003_viborita;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.LinkedList;

public class SnakeView extends View {
    private GestureDetector gestos;
    private Direccion direccion;
    private LinkedList<Punto> lista;
    private Handler manejador=new Handler(Looper.getMainLooper());;
    private Runnable tiempo;
    private int columna, fila; // columna y fila donde se encuentra la cabeza de la vibora
    private int colfruta, filfruta; // columna y fila donde se encuentra la fruta
    private boolean activo = true; // disponemos en false cuando finaliza el juego
    private int crecimiento = 0; // indica la cantidad de cuadraditos que debe crecer la vibora
    private int ladoCuadradito;
    private int cuadrosAncho=30,cuadrosAlto;//cuadrosAlto se calcula según la altura del dispositivo

    private enum Direccion {
        IZQUIERDA, DERECHA, SUBE, BAJA
    };

    class Punto {
        int x, y;

        public Punto(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public SnakeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        gestos=new GestureDetector(this.getContext(),new EscuchaGestos());
        tiempo=new Runnable() {
            @Override
            public void run() {
                switch (direccion) {
                    case DERECHA:
                        columna++;
                        break;
                    case IZQUIERDA:
                        columna--;
                        break;
                    case SUBE:
                        fila--;
                        break;
                    case BAJA:
                        fila++;
                        break;
                }
                sePisa();
                // insertamos la coordenada de la cabeza de la vibora en la lista
                lista.addFirst(new Punto(columna, fila));

                if (verificarComeFruta() == false && crecimiento == 0) {
                    // si no estamos en la coordenada de la fruta y no debe crecer la vibora
                    // borramos el ultimo nodo de la lista
                    // esto hace que la lista siga teniendo la misma cantidad de nodos
                    lista.remove(lista.size() - 1);
                } else {
                    // Si creciento es mayor a cero es que debemos hacer crecer la vibora
                    if (crecimiento > 0)
                        crecimiento--;
                }
                verificarFueraTablero();
                invalidate();
                if (activo)
                    manejador.postDelayed(this,100);
            }
        };
        iniciar();

    }

    public void iniciar()
    {
        lista = new LinkedList<Punto>();
        direccion = Direccion.DERECHA;
        crecimiento = 0;
        activo = true;
        lista.add(new Punto(4, 5));
        lista.add(new Punto(3, 5));
        lista.add(new Punto(2, 5));
        lista.add(new Punto(1, 5));
        // indicamos la ubicacion de la cabeza de la vibora
        columna = 4;
        fila = 5;
        generarCoordenadaFruta();
        manejador.removeCallbacksAndMessages(null);
        manejador.postDelayed(tiempo,100);
    }


    // controlamos si la cabeza de la vibora se encuentra dentro de su cuerpo
    private void sePisa() {
        for (Punto p : lista) {
            if (p.x == columna && p.y == fila) {
                activo = false;
            }
        }
    }

    private boolean verificarComeFruta() {
        if (columna == colfruta && fila == filfruta) {
            generarCoordenadaFruta();
            crecimiento = 10;
            return true;
        } else
            return false;
    }

    private void generarCoordenadaFruta()
    {
        // generamos la coordenada de la fruta
        colfruta = 3+(int) (Math.random() * (cuadrosAncho-4));
        filfruta = 4+(int) (Math.random() * (cuadrosAlto-4));

    }


    // controlamos si estamos fuera de la region del tablero
    private void verificarFueraTablero() {
        if (columna <= 0 || columna >= cuadrosAncho || fila <= 0 || fila >= cuadrosAlto) {
            activo = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics displaymetrics = getResources(). getDisplayMetrics();
        ladoCuadradito = displaymetrics.widthPixels/cuadrosAncho;
        ladoCuadradito = displaymetrics.widthPixels/cuadrosAncho;
        cuadrosAlto=displaymetrics.heightPixels/ ladoCuadradito;

        //Obligatoriamente debemos llamar a este metodo
        setMeasuredDimension(displaymetrics.widthPixels, displaymetrics.heightPixels);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestos.onTouchEvent(event);
        return true;
    }

    class EscuchaGestos extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float ancho=Math.abs(e2.getX()- e1.getX());
            float alto=Math.abs(e2.getY()-e1.getY());
            if (ancho>alto) {
                if (e2.getX() > e1.getX()) {
                    direccion = Direccion.DERECHA;
                } else {
                    direccion = Direccion.IZQUIERDA;
                }
            }
            else {
                if (e2.getY() > e1.getY()) {
                    direccion = Direccion.BAJA;
                } else {
                    direccion = Direccion.SUBE;
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            iniciar();
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRGB(163,188,182);
        Paint pincel1=new Paint();
        pincel1.setColor(Color.WHITE);
        for(int c=0;c<=cuadrosAncho+1;c++)
            canvas.drawLine(c* ladoCuadradito,0,
                    c* ladoCuadradito,cuadrosAlto*ladoCuadradito,pincel1);
        for(int f=0;f<=cuadrosAlto;f++)
            canvas.drawLine(0,f*ladoCuadradito,
                    cuadrosAncho* ladoCuadradito +cuadrosAncho,f*ladoCuadradito,pincel1);

        pincel1.setColor(Color.rgb(57, 96, 61));
        for (Punto punto : lista) {
            canvas.drawRect(punto.x * ladoCuadradito, punto.y * ladoCuadradito,
                    punto.x * ladoCuadradito + ladoCuadradito -3,  punto.y * ladoCuadradito+ladoCuadradito-3,pincel1);
        }
        // dibujar fruta
        pincel1.setColor(Color.BLUE);
        canvas.drawRect(colfruta* ladoCuadradito, filfruta*ladoCuadradito,
                colfruta* ladoCuadradito + ladoCuadradito, filfruta*ladoCuadradito+ladoCuadradito,pincel1);
    }


}
