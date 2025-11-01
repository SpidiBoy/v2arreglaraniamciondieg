package Objetos;

import GameGFX.Animacion;
import Objetos.Utilidad.Handler;
import Objetos.Utilidad.ObjetosID;
import mariotest.Mariotest;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Clase DiegoKong con animación de agarrar princesa CORREGIDA
 */
public class DiegoKong extends GameObjetos {
    
    private static final float WIDTH = 48;
    private static final float HEIGHT = 32;
    private static final float HEIGHT_AGARRA_PRINCESA = 40; // Nuevo sprite más alto
    
    private Handler handler;
    private BufferedImage[] dkSprites;
    
    // Animaciones
    private Animacion dkReposo;
    private Animacion dkAgarra;
    private Animacion dkLanza;
    private Animacion dkGolpeaPecho;
    private Animacion dkAgarraPrincesa;
    
    private Animacion animacionActual;
    
    // Estados
    private EstadoDK estado;
    private boolean mirandoDerecha = true;
    
    // Control de lanzamiento
    private int ticksDesdeUltimoLanzamiento = 0;
    private int ticksEntrelanzamientos = 180;
    private boolean preparandoLanzamiento = false;
    private int ticksAnimacionLanzar = 0;
    private static final int DURACION_ANIMACION_LANZAR = 30;
    
    private float offsetBarrilX = 20f;
    private float offsetBarrilY = 10f;
    
    private static final int TICKS_MIN_LANZAMIENTO = 120;
    private static final int TICKS_MAX_LANZAMIENTO = 240;
    
    public enum EstadoDK {
        REPOSO,
        AGARRANDO,
        LANZANDO,
        GOLPEANDO_PECHO,
        ENOJADO,
        AGARRANDO_PRINCESA
    }
    
    public DiegoKong(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.DiegoKong, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.estado = EstadoDK.REPOSO;
        
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[DIEGO KONG] Creado en (" + x + ", " + y + ")");
    }
    
    private void cargarSprites() {
        try {
            dkSprites = Mariotest.getTextura().getDiegoKongSprites();
            
            if (dkSprites != null && dkSprites.length > 0) {
                System.out.println("[DIEGO KONG] Sprites cargados: " + dkSprites.length);
            } else {
                System.err.println("[ERROR] Los sprites de Diego Kong no se cargaron o el array está vacío.");
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudieron cargar sprites de Diego Kong: " + e.getMessage());
        }
    }
    
    private void inicializarAnimaciones() {
        if (dkSprites == null || dkSprites.length < 8) {
            System.err.println("[ERROR] No hay suficientes sprites para las animaciones de DK.");
            return;
        }
        
        // Fila 1 (Índices 0-3)
        dkReposo = new Animacion(15, dkSprites[0], dkSprites[1]);
        dkGolpeaPecho = new Animacion(8, dkSprites[2], dkSprites[3], dkSprites[2]);
        
        // Fila 2 (Índices 4-7)
        dkAgarra = new Animacion(8, dkSprites[5], dkSprites[5]);
        dkLanza = new Animacion(6, dkSprites[4], dkSprites[4], dkSprites[6], dkSprites[6]);
        
        // ✅ ANIMACIÓN DE AGARRAR PRINCESA (sprites especiales 48x40)
        BufferedImage[] spritesAgarrar = Mariotest.getTextura().getDKAgarraSprites();
        if (spritesAgarrar != null && spritesAgarrar.length >= 3) {
            // 🔍 VERIFICAR si los sprites NO están vacíos
            boolean spritesValidos = true;
            for (int i = 0; i < spritesAgarrar.length; i++) {
                if (spritesAgarrar[i] == null) {
                    System.err.println("[DK] ❌ Sprite " + i + " es NULL");
                    spritesValidos = false;
                    break;
                }
                
                // Verificar dimensiones correctas
                if (spritesAgarrar[i].getWidth() != 48 || spritesAgarrar[i].getHeight() != 40) {
                    System.err.println("[DK] ⚠️ Sprite " + i + " tiene dimensiones incorrectas: " + 
                        spritesAgarrar[i].getWidth() + "x" + spritesAgarrar[i].getHeight());
                }
            }
            
            if (spritesValidos) {
                // 🔥 VELOCIDAD AJUSTADA: más lenta para que se vea bien
                dkAgarraPrincesa = new Animacion(12, 
                    spritesAgarrar[0], 
                    spritesAgarrar[1], 
                    spritesAgarrar[2]
                );
                System.out.println("[DIEGO KONG] ✅ Animación de agarrar princesa cargada (48x40, " + spritesAgarrar.length + " frames)");
            } else {
                // Fallback: usar animación de agarrar barril
                dkAgarraPrincesa = dkAgarra;
                System.out.println("[DIEGO KONG] ⚠️ Sprites inválidos, usando animación de agarrar como fallback");
            }
        } else {
            // Fallback: usar animación de agarrar barril
            dkAgarraPrincesa = dkAgarra;
            System.out.println("[DIEGO KONG] ⚠️ No se cargaron sprites de agarrar, usando fallback");
        }
        
        animacionActual = dkReposo;
        
        System.out.println("[DIEGO KONG] Animaciones inicializadas correctamente.");
    }

    @Override
    public void tick() {
        // 🔥 CRÍTICO: Ejecutar animación SIEMPRE
        if (animacionActual != null) {
            animacionActual.runAnimacion();
        }
        
        switch (estado) {
            case REPOSO:
                tickReposo();
                break;
                
            case AGARRANDO:
                tickAgarrando();
                break;
                
            case LANZANDO:
                tickLanzando();
                break;
                
            case GOLPEANDO_PECHO:
                tickGolpeandoPecho();
                break;
                
            case ENOJADO:
                tickEnojado();
                break;
                
            case AGARRANDO_PRINCESA:
                tickAgarrandoPrincesa();
                break;
        }
        
        verificarProximidadJugador();
    }
    
    private void tickReposo() {
        animacionActual = dkReposo;
        ticksDesdeUltimoLanzamiento++;
        
        if (ticksDesdeUltimoLanzamiento >= ticksEntrelanzamientos) {
            iniciarLanzamiento();
        }
    }
    
    private void tickAgarrando() {
        animacionActual = dkAgarra;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 15) {
            estado = EstadoDK.LANZANDO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    private void tickLanzando() {
        animacionActual = dkLanza;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= DURACION_ANIMACION_LANZAR) {
            ticksAnimacionLanzar = 0;
            ticksDesdeUltimoLanzamiento = 0;
            
            if (Math.random() < 0.3) {
                estado = EstadoDK.GOLPEANDO_PECHO;
            } else {
                estado = EstadoDK.REPOSO;
            }
            
            ticksEntrelanzamientos = TICKS_MIN_LANZAMIENTO + 
                (int)(Math.random() * (TICKS_MAX_LANZAMIENTO - TICKS_MIN_LANZAMIENTO));
        }
    }
    
    private void tickGolpeandoPecho() {
        animacionActual = dkGolpeaPecho;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 40) {
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    private void tickEnojado() {
        animacionActual = dkGolpeaPecho;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 60) {
            iniciarLanzamiento();
            ticksAnimacionLanzar = 0;
        }
        
        if (!jugadorCerca()) {
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    // 🔥 CORREGIDO: Estado de agarrar princesa con animación activa
    private void tickAgarrandoPrincesa() {
        // ✅ Cambiar a la animación correcta
        animacionActual = dkAgarraPrincesa;
        ticksAnimacionLanzar++; // Mantiene la animación viva
        
        // Debug cada 60 ticks
         if (ticksAnimacionLanzar % 60 == 0) {
             System.out.println("[DK] 🦍 Agarrando princesa... tick: " + ticksAnimacionLanzar);
         }
    }
    
    private void iniciarLanzamiento() {
        estado = EstadoDK.AGARRANDO;
        ticksAnimacionLanzar = 0;
        System.out.println("[DIEGO KONG] Iniciando lanzamiento de barril...");
    }
    
    private void verificarProximidadJugador() {
        Player player = handler.getPlayer();
        if (player == null) return;
        
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        if (distanciaX < 200 && distanciaY < 100 && estado == EstadoDK.REPOSO) {
            if (Math.random() < 0.1) {
                estado = EstadoDK.ENOJADO;
                ticksAnimacionLanzar = 0;
                System.out.println("[DIEGO KONG] ¡Se ha puesto ENOJADO!");
            }
        }
        
        mirandoDerecha = player.getX() > getX();
    }
    
    private boolean jugadorCerca() {
        Player player = handler.getPlayer();
        if (player == null) return false;
        
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        return distanciaX < 200 && distanciaY < 100;
    }
    
    // ✅ CORREGIDO: Activa la animación de agarrar princesa
    public void activarAnimacionAgarrar() {
        estado = EstadoDK.AGARRANDO_PRINCESA;
        ticksAnimacionLanzar = 0;
        animacionActual = dkAgarraPrincesa; // 🔥 ASEGURAR que use la animación correcta
        System.out.println("[DIEGO KONG] 🦍 Activando animación de agarrar princesa");
    }
    
    public void volverAReposo() {
        estado = EstadoDK.REPOSO;
        ticksAnimacionLanzar = 0;
        animacionActual = dkReposo;
    }
    
    @Override
    public void aplicarGravedad() {
        // Diego Kong no tiene gravedad
    }

    @Override
    public void render(Graphics g) {
        // 🔥 Se recomienda ELIMINAR O COMENTAR este bloque en la versión final:
        // BufferedImage[] spritesDebug = Mariotest.getTextura().getDKAgarraSprites();
        // if (estado == EstadoDK.AGARRANDO_PRINCESA) {
        //     System.out.println("[DK RENDER DEBUG] Sprites DK Agarra: " + 
        //         (spritesDebug != null ? spritesDebug.length + " sprites" : "NULL") +
        //         " | AnimacionActual: " + (animacionActual != null ? "OK" : "NULL"));
        // }
        
        if (animacionActual != null) {
            
            // ==================== RENDERIZADO CORREGIDO ====================
            int renderWidth = (int) getWidth();
            int renderHeight = (int) getHeight();
            int renderY = (int) getY();

            // 🔥 Si está agarrando princesa, usar sprites de 48x40
            if (estado == EstadoDK.AGARRANDO_PRINCESA) {
                renderHeight = (int) HEIGHT_AGARRA_PRINCESA;
                // Ajustar Y para que la base del sprite (plataforma) se mantenga
                renderY = (int) (getY() - (HEIGHT_AGARRA_PRINCESA - HEIGHT));
                
                // 🔥 FALLBACK: Si los sprites están mal, usar animación normal
                BufferedImage[] spritesDebug = Mariotest.getTextura().getDKAgarraSprites();
                if (spritesDebug == null || spritesDebug.length == 0 || spritesDebug[0] == null) {
                    System.err.println("[DK RENDER] ⚠️ Sprites de agarrar INVÁLIDOS - usando animación normal");
                    animacionActual = dkReposo;
                    renderHeight = (int) getHeight();
                    renderY = (int) getY();
                }
            }
            
            // Renderizar según dirección
            if (mirandoDerecha) {
                animacionActual.drawAnimacion(g, 
                    (int) getX(), renderY, 
                    renderWidth, renderHeight
                );
            } else {
                animacionActual.drawAnimacion(g, 
                    (int) (getX() + renderWidth), renderY, 
                    -renderWidth, renderHeight
                );
            }
            
        } else {
            // Placeholder si no hay animación
            g.setColor(new Color(89, 47, 20));
            g.fillRect((int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight());
            
            g.setColor(Color.YELLOW);
            g.drawString("DK", (int) getX() + 15, (int) getY() - 5);
        }
        
        // 🔥 RENDERIZADO DE EMERGENCIA si está transparente (Se recomienda ELIMINAR/COMENTAR)
        // if (estado == EstadoDK.AGARRANDO_PRINCESA) {
        //     g.setColor(new Color(255, 0, 0, 100)); // Rojo semi-transparente para debug
        //     g.fillRect((int) getX(), (int) getY(), (int) getWidth(), (int) getHeight());
        //     g.setColor(Color.WHITE);
        //     g.drawString("AGARRANDO", (int) getX() - 10, (int) getY() - 10);
        // }
    }

    /**
     * ✅ CORRECCIÓN CRÍTICA: Ajusta el hitbox para que coincida 
     * con la altura de 40px del sprite AGARRANDO_PRINCESA.
     */
    @Override
    public Rectangle getBounds() {
        if (estado == EstadoDK.AGARRANDO_PRINCESA) {
            // La altura del hitbox es 40px
            float boundsHeight = HEIGHT_AGARRA_PRINCESA;
            
            // La posición Y del hitbox debe moverse hacia arriba 8px 
            // (40-32=8) para mantener la base alineada.
            float boundsY = getY() - (HEIGHT_AGARRA_PRINCESA - HEIGHT);
            
            return new Rectangle(
                (int) getX(),
                (int) boundsY,
                (int) WIDTH,
                (int) boundsHeight
            );
        }
        
        // Para todos los demás estados, usa la altura y posición base (48x32)
        return new Rectangle(
            (int) getX(),
            (int) getY(),
            (int) getWidth(),
            (int) getHeight()
        );
    }
    
    public void forzarLanzamiento() {
        if (estado == EstadoDK.REPOSO) {
            iniciarLanzamiento();
        }
    }
    
    public void setVelocidadLanzamiento(int ticksMin, int ticksMax) {
        if (ticksMin > 0 && ticksMax > ticksMin) {
            ticksEntrelanzamientos = ticksMin + 
                (int)(Math.random() * (ticksMax - ticksMin));
        }
    }
    
    public void activarModoEnojado() {
        if (estado == EstadoDK.REPOSO) {
            estado = EstadoDK.ENOJADO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    public EstadoDK getEstado() {
        return estado;
    }
    
    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }
    
    public void setMirandoDerecha(boolean mirandoDerecha) {
        this.mirandoDerecha = mirandoDerecha;
    }
    
    public int getTicksParaProximoLanzamiento() {
        return ticksEntrelanzamientos - ticksDesdeUltimoLanzamiento;
    }
}