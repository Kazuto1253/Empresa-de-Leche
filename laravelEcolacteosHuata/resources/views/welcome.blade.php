<!DOCTYPE html>
<html class="scroll-smooth" lang="es">
<head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Ecolácteos Huata | Acopio, Producción y Ventas</title>

<link rel="icon" href="/favicon.ico" sizes="any">
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="apple-touch-icon" href="/apple-touch-icon.png">

<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Epilogue:wght@500;600;700;800&family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<script id="tailwind-config">
      tailwind.config = {
        darkMode: "class",
        theme: {
          extend: {
            "colors": {
                    "primary": "#1a8049",
                    "on-primary": "#ffffff",
                    "primary-container": "#d6f4de",
                    "on-primary-container": "#12432c",
                    "primary-fixed": "#ade9c0",
                    "on-primary-fixed": "#07261a",
                    "primary-fixed-dim": "#7ad89d",
                    "on-primary-fixed-variant": "#155134",
                    "inverse-primary": "#7ad89d",

                    "secondary": "#0165a5",
                    "on-secondary": "#ffffff",
                    "secondary-container": "#def0ff",
                    "on-secondary-container": "#0b4770",
                    "secondary-fixed": "#b6e4ff",
                    "on-secondary-fixed": "#052840",
                    "secondary-fixed-dim": "#77d1ff",
                    "on-secondary-fixed-variant": "#075587",

                    "tertiary": "#d99400",
                    "on-tertiary": "#2a1c00",
                    "tertiary-container": "#ffe9b3",
                    "on-tertiary-container": "#4a3300",
                    "tertiary-fixed": "#ffd23f",
                    "on-tertiary-fixed": "#2a1c00",
                    "tertiary-fixed-dim": "#f7b500",
                    "on-tertiary-fixed-variant": "#4a3300",

                    "error": "#c31f1f",
                    "on-error": "#ffffff",
                    "error-container": "#ffdad6",
                    "on-error-container": "#93000a",

                    "background": "#fbfaf6",
                    "on-background": "#1a2420",
                    "surface": "#fbfaf6",
                    "on-surface": "#1a2420",
                    "surface-dim": "#dcd9d2",
                    "surface-bright": "#fbfaf6",
                    "surface-container-lowest": "#ffffff",
                    "surface-container-low": "#f5f3ee",
                    "surface-container": "#efece6",
                    "surface-container-high": "#e9e6e0",
                    "surface-container-highest": "#e3e0da",
                    "surface-variant": "#e1e4de",
                    "on-surface-variant": "#44483f",
                    "surface-tint": "#1a8049",

                    "outline": "#75796d",
                    "outline-variant": "#c5c9bc",
                    "inverse-surface": "#2f332c",
                    "inverse-on-surface": "#f1f1ea"
            },
            "borderRadius": {
                    "DEFAULT": "0.25rem",
                    "lg": "0.5rem",
                    "xl": "0.75rem",
                    "full": "9999px"
            },
            "spacing": {
                    "lg": "48px",
                    "md": "24px",
                    "sm": "12px",
                    "xs": "4px",
                    "margin-desktop": "64px",
                    "base": "8px",
                    "xl": "80px",
                    "margin-mobile": "16px",
                    "gutter": "24px"
            },
            "fontFamily": {
                    "display-lg": ["Epilogue", "ui-sans-serif", "system-ui", "sans-serif"],
                    "body-lg": ["Be Vietnam Pro", "ui-sans-serif", "system-ui", "sans-serif"],
                    "label-md": ["Be Vietnam Pro", "ui-sans-serif", "system-ui", "sans-serif"],
                    "headline-lg-mobile": ["Epilogue", "ui-sans-serif", "system-ui", "sans-serif"],
                    "headline-lg": ["Epilogue", "ui-sans-serif", "system-ui", "sans-serif"],
                    "body-md": ["Be Vietnam Pro", "ui-sans-serif", "system-ui", "sans-serif"],
                    "headline-md": ["Epilogue", "ui-sans-serif", "system-ui", "sans-serif"]
            },
            "fontSize": {
                    "display-lg": ["48px", {"lineHeight": "1.1", "letterSpacing": "-0.02em", "fontWeight": "700"}],
                    "body-lg": ["18px", {"lineHeight": "1.6", "fontWeight": "400"}],
                    "label-md": ["14px", {"lineHeight": "1.4", "letterSpacing": "0.05em", "fontWeight": "600"}],
                    "headline-lg-mobile": ["28px", {"lineHeight": "1.2", "fontWeight": "600"}],
                    "headline-lg": ["32px", {"lineHeight": "1.2", "letterSpacing": "-0.01em", "fontWeight": "600"}],
                    "body-md": ["16px", {"lineHeight": "1.6", "fontWeight": "400"}],
                    "headline-md": ["24px", {"lineHeight": "1.3", "fontWeight": "600"}]
            }
          },
        },
      }
    </script>
<style>
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
        }
        .reveal {
            opacity: 0;
            transform: translateY(40px);
            transition: opacity 1s cubic-bezier(0.4, 0, 0.2, 1), transform 1s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .reveal.active {
            opacity: 1;
            transform: translateY(0);
        }
        .stagger-load {
            opacity: 0;
            transform: translateY(20px);
            animation: staggerIn 0.8s cubic-bezier(0.2, 0, 0.2, 1) forwards;
        }
        @keyframes staggerIn {
            to { opacity: 1; transform: translateY(0); }
        }
        .pulse-soft {
            animation: pulseSoft 3s ease-in-out infinite;
        }
        @keyframes pulseSoft {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.05); opacity: 0.9; }
        }
        .glass-nav-scrolled {
            background-color: rgba(251, 250, 246, 0.95);
            box-shadow: 0 4px 30px rgba(0, 0, 0, 0.05);
        }

        /* === Navbar glass responsiva === */
        .main-nav-glass {
            width: 90%;
            height: 90px;
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 999;
            background: rgba(255, 255, 255, 0.12);
            backdrop-filter: blur(20px);
            border-radius: 25px;
            border: 1px solid rgba(255, 255, 255, 0.25);
            box-shadow: 0 8px 30px rgba(18, 67, 44, 0.25);
            transition: height 0.3s ease, border-radius 0.3s ease;
        }
        @media (max-width: 767px) {
            .main-nav-glass {
                height: 64px;
                top: 12px;
                width: calc(100% - 24px);
                border-radius: 20px;
            }
            .main-nav-glass.nav-open { height: auto; }
        }

        /* Menú móvil colapsable dentro de la navbar */
        #mobile-menu {
            max-height: 0;
            overflow: hidden;
            opacity: 0;
            padding: 0 1rem;
            transition: max-height 0.35s ease, opacity 0.25s ease, padding 0.35s ease;
        }
        #mobile-menu.is-open {
            max-height: 340px;
            opacity: 1;
            padding: 0.75rem 1rem 1.25rem;
            border-top: 1px solid rgba(255, 255, 255, 0.2);
        }
        #mobile-menu-btn .icon-close { display: none; }
        #mobile-menu-btn.is-open .icon-menu { display: none; }
        #mobile-menu-btn.is-open .icon-close { display: inline; }

        /* === Espaciado y tipografía fluida === */
        .pad-x {
            padding-left: clamp(16px, 4vw, 64px);
            padding-right: clamp(16px, 4vw, 64px);
        }
        .pad-y {
            padding-top: clamp(48px, 7vw, 80px);
            padding-bottom: clamp(48px, 7vw, 80px);
        }
        .hero-pad-top { padding-top: clamp(96px, 13vw, 140px); }
        .container-max { width: 100%; max-width: 80rem; margin-left: auto; margin-right: auto; }

        .fluid-display     { font-size: clamp(2rem, 6.5vw, 3rem); line-height: 1.1; letter-spacing: -0.02em; }
        .fluid-headline-lg { font-size: clamp(1.5rem, 4vw, 2rem); line-height: 1.2; }
        .fluid-headline-md { font-size: clamp(1.125rem, 2.5vw, 1.5rem); line-height: 1.3; }
        .fluid-body        { font-size: clamp(0.95rem, 1.4vw, 1.05rem); line-height: 1.6; }

        body { overflow-x: hidden; }
    </style>
</head>
<body class="bg-background text-on-background font-body-md selection:bg-primary-container selection:text-on-primary-container">
<!-- WebGL Shader Background -->
<div class="fixed inset-0 w-full h-full -z-10" style="display:block;">
<canvas id="shader-canvas-huata" style="display:block;width:100%;height:100%"></canvas>
<script>
(function() {
  const canvas = document.getElementById('shader-canvas-huata');

  function syncSize() {
    const w = canvas.clientWidth  || 1280;
    const h = canvas.clientHeight || 720;
    if (canvas.width !== w || canvas.height !== h) {
      canvas.width  = w;
      canvas.height = h;
    }
  }
  if (typeof ResizeObserver !== 'undefined') {
    new ResizeObserver(syncSize).observe(canvas);
  }
  syncSize();

  const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
  if (!gl) return;
  const vs = `attribute vec2 a_position;
varying vec2 v_texCoord;
void main() {
  v_texCoord = a_position * 0.5 + 0.5;
  gl_Position = vec4(a_position, 0.0, 1.0);
}`;
  const fs = `precision highp float;
varying vec2 v_texCoord;
uniform float u_time;
uniform vec2 u_resolution;
uniform vec2 u_mouse;

// Paleta Ecolácteos Huata: crema suave + verde pastizal
void main() {
    vec2 uv = v_texCoord;
    vec2 mouse = u_mouse / u_resolution;

    float noise = 0.0;
    vec2 p = uv * 3.0;
    float t = u_time * 0.4;

    for(int i = 1; i < 5; i++) {
        float fi = float(i);
        p += sin(p.yx * 1.5 + t + fi) * 0.5;
        noise += sin(p.x + p.y + t) / fi;
    }

    vec3 color1 = vec3(0.984, 0.980, 0.965); // #fbfaf6 crema suave
    vec3 color2 = vec3(0.678, 0.914, 0.753); // #ade9c0 verde pastizal

    float dist = distance(uv, mouse);
    float ripple = sin(dist * 10.0 - t * 2.0) * 0.02;

    float mixFactor = smoothstep(-1.0, 1.0, noise + ripple);
    vec3 finalColor = mix(color1, color2, mixFactor);

    finalColor += noise * 0.03;

    gl_FragColor = vec4(finalColor, 1.0);
}`;
  function cs(type, src) {
    const s = gl.createShader(type);
    gl.shaderSource(s, src);
    gl.compileShader(s);
    return s;
  }
  const prog = gl.createProgram();
  gl.attachShader(prog, cs(gl.VERTEX_SHADER, vs));
  gl.attachShader(prog, cs(gl.FRAGMENT_SHADER, fs));
  gl.linkProgram(prog);
  gl.useProgram(prog);
  const buf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, buf);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1,-1, 1,-1, -1,1, 1,1]), gl.STATIC_DRAW);
  const pos = gl.getAttribLocation(prog, 'a_position');
  gl.enableVertexAttribArray(pos);
  gl.vertexAttribPointer(pos, 2, gl.FLOAT, false, 0, 0);
  const uTime = gl.getUniformLocation(prog, 'u_time');
  const uRes = gl.getUniformLocation(prog, 'u_resolution');
  const uMouse = gl.getUniformLocation(prog, 'u_mouse');

  let mouse = { x: canvas.width / 2, y: canvas.height / 2 };
  window.addEventListener('mousemove', (event) => {
    const rect = canvas.getBoundingClientRect();
    if (rect.width && rect.height) {
      const nx = (event.clientX - rect.left) / rect.width;
      const ny = 1.0 - (event.clientY - rect.top) / rect.height;
      mouse.x = nx * canvas.width;
      mouse.y = ny * canvas.height;
    }
  });

  function render(t) {
    if (typeof ResizeObserver === 'undefined') syncSize();
    gl.viewport(0, 0, canvas.width, canvas.height);
    if (uTime) gl.uniform1f(uTime, t * 0.001);
    if (uRes) gl.uniform2f(uRes, canvas.width, canvas.height);
    if (uMouse) gl.uniform2f(uMouse, mouse.x, mouse.y);
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
    requestAnimationFrame(render);
  }
  render(0);
})();
</script>
</div>

<!-- TopNavBar -->
<nav id="main-nav" class="main-nav-glass">
    <div class="flex justify-between items-center h-[64px] md:h-[90px] px-4 md:px-8 max-w-7xl mx-auto">
        <a href="#inicio" class="flex items-center gap-2 md:gap-3 min-w-0">
            <img src="{{ asset('images/logo-huata.jpg') }}" alt="Ecolácteos Huata" class="w-9 h-9 md:w-10 md:h-10 shrink-0 rounded-full object-cover bg-white border-2 border-primary-container shadow-md">
            <span class="font-display-lg text-lg md:text-headline-md text-primary font-bold truncate">Ecolácteos Huata</span>
        </a>
        <div class="hidden md:flex items-center space-x-lg">
            <a class="nav-section-link text-primary font-bold border-b-2 border-primary pb-1" href="#inicio">Inicio</a>
            <a class="nav-section-link text-primary font-bold border-b-2 border-primary pb-1" href="#somos">Quiénes somos</a>
            <a class="nav-section-link text-primary font-bold border-b-2 border-primary pb-1" href="#proceso">Cómo trabajamos</a>
            <a class="nav-section-link text-primary font-bold border-b-2 border-primary pb-1" href="#contacto">Contacto</a>
        </div>
        <div class="flex items-center gap-2">
            <a href="{{ route('login') }}" class="hidden md:inline-block bg-primary text-on-primary font-bold px-7 py-3 rounded-full shadow-lg shadow-primary/40 ring-2 ring-white/40 hover:shadow-xl hover:shadow-primary/50 hover:scale-105 active:scale-95 transition-all">Iniciar sesión</a>
            <button type="button" id="mobile-menu-btn"
                    class="md:hidden flex items-center justify-center w-10 h-10 rounded-full border border-white/30 text-primary bg-white/20 hover:bg-white/30 transition-colors"
                    aria-label="Abrir menú" aria-expanded="false" aria-controls="mobile-menu">
                <span class="material-symbols-outlined icon-menu text-2xl">menu</span>
                <span class="material-symbols-outlined icon-close text-2xl">close</span>
            </button>
        </div>
    </div>
    <div id="mobile-menu" class="md:hidden">
        <div class="flex flex-col gap-1">
            <a class="nav-section-link text-primary font-bold border-l-4 border-primary pl-3 py-2.5 rounded-r-lg" href="#inicio">Inicio</a>
            <a class="nav-section-link text-on-surface-variant font-semibold pl-3 py-2.5 rounded-r-lg hover:text-primary transition-colors" href="#somos">Quiénes somos</a>
            <a class="nav-section-link text-on-surface-variant font-semibold pl-3 py-2.5 rounded-r-lg hover:text-primary transition-colors" href="#proceso">Cómo trabajamos</a>
            <a class="nav-section-link text-on-surface-variant font-semibold pl-3 py-2.5 rounded-r-lg hover:text-primary transition-colors" href="#contacto">Contacto</a>
            <a href="{{ route('login') }}" class="mt-2 bg-primary text-on-primary text-center px-6 py-3.5 rounded-full shadow-lg shadow-primary/40 hover:scale-[0.98] transition-transform font-bold text-base">Iniciar sesión</a>
        </div>
    </div>
</nav>

<!-- Hero Section -->
<section class="relative min-h-screen flex items-center hero-pad-top pb-12 sm:pb-16" id="inicio">
    <div class="absolute inset-0 z-0 overflow-hidden">
        <img class="w-full h-full object-cover brightness-95 scale-105"
             src="{{ asset('images/fondo-huata.jpg') }}" alt="Fondo Ecolácteos Huata">
        <div class="absolute inset-0 bg-gradient-to-b md:bg-gradient-to-r from-on-primary-fixed/70 via-on-primary-fixed/30 md:from-on-primary-fixed/80 md:via-on-primary-fixed/40 to-background/80"></div>
    </div>
    <div class="relative z-10 pad-x container-max w-full flex justify-center md:justify-start">
        <div class="w-full max-w-2xl text-center md:text-left">
            <p class="text-tertiary-fixed-dim font-label-md text-label-md uppercase tracking-widest mb-base stagger-load"
               style="animation-delay: 100ms;">
                Asociación de Productores de Huata, Puno
            </p>
            <h1 class="font-display-lg fluid-display text-white mb-md leading-tight stagger-load"
                style="animation-delay: 300ms;">
                <span class="text-white">Ecolácteos</span> <span class="text-primary-fixed">Huata</span>
            </h1>
            <p class="font-body-md fluid-body text-white/90 mb-lg max-w-xl mx-auto md:mx-0 stagger-load" style="animation-delay: 500ms;">
                Acopio, producción y venta de derivados lácteos, hechos con la leche fresca de las familias productoras de Huata.
            </p>
            <div class="flex flex-col sm:flex-row justify-center md:justify-start items-stretch sm:items-center gap-3 sm:gap-md stagger-load"
                 style="animation-delay: 700ms;">
                <a href="{{ route('login') }}"
                   class="bg-primary text-on-primary font-bold px-6 sm:px-xl py-3.5 sm:py-md rounded-full font-label-md text-label-md text-base sm:text-lg shadow-xl shadow-primary/50 ring-2 ring-white/50 hover:shadow-2xl hover:shadow-primary/60 transition-all hover:-translate-y-1 text-center">
                    Iniciar sesión
                </a>
                <a href="#somos"
                   class="border-2 border-white bg-white/10 text-white font-bold px-6 sm:px-xl py-3.5 sm:py-md rounded-full font-label-md text-label-md text-base sm:text-lg backdrop-blur-sm shadow-lg hover:bg-white/25 transition-all hover:-translate-y-1 text-center">
                    Conócenos
                </a>
            </div>
        </div>
    </div>
</section>

<!-- Somos Ecolácteos Huata Section -->
<section class="pad-y pad-x bg-surface-container-low/50 overflow-hidden" id="somos">
    <div class="container-max grid grid-cols-1 md:grid-cols-2 gap-8 sm:gap-12 md:gap-xl items-center">
        <div class="reveal">
            <div class="relative group max-w-md mx-auto md:max-w-none">
                <div class="absolute -top-4 -left-4 w-20 h-20 sm:w-24 sm:h-24 bg-primary-container rounded-full mix-blend-multiply opacity-40 animate-pulse"></div>
                <img class="rounded-2xl shadow-xl relative z-10 w-full aspect-[4/5] object-cover transition-transform duration-700 group-hover:scale-[1.02]"
                     alt="Somos Ecolácteos Huata"
                     src="{{ asset('images/fondo-huata.jpg') }}"/>
            </div>
        </div>
        <div class="reveal text-center md:text-left" style="transition-delay: 200ms;">
            <h2 class="font-display-lg fluid-headline-lg text-on-background mb-md">Somos Ecolácteos Huata</h2>
            <div class="w-16 h-1 bg-primary mb-lg mx-auto md:mx-0"></div>
            <p class="font-body-md fluid-body text-on-surface-variant leading-relaxed">
                Somos una asociación de productores lecheros de la comunidad de Huata, Puno. Acopiamos la leche fresca
                de nuestros proveedores todos los días, la transformamos en queso, yogurt y otros derivados en nuestra
                planta, y la vendemos directamente a nuestra comunidad — cuidando la calidad en cada paso, desde el
                ordeño hasta el producto final.
            </p>
        </div>
    </div>
</section>

<!-- Cómo trabajamos Section -->
<section class="pad-y pad-x bg-background/30 backdrop-blur-sm" id="proceso">
<div class="container-max text-center mb-12 sm:mb-xl reveal">
<h2 class="font-display-lg fluid-headline-lg text-on-background mb-sm">CÓMO TRABAJAMOS</h2>
<p class="font-body-md fluid-body text-on-surface-variant max-w-2xl mx-auto px-2">
    De la finca a la mesa: un proceso pensado para cuidar la calidad de la leche y el bienestar de nuestros productores.
</p>
</div>
<div class="container-max grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-md">
<!-- Card Acopio -->
<div class="relative p-md rounded-xl bg-primary overflow-hidden hover:shadow-2xl hover:-translate-y-2 transition-all reveal group">
    <div class="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center mb-md pulse-soft group-hover:scale-110 transition-transform">
        <span class="material-symbols-outlined text-white text-2xl">local_shipping</span>
    </div>
    <h3 class="font-headline-md text-headline-md text-white mb-sm">Acopio</h3>
    <p class="font-body-md text-body-md text-white/90">
        Recogemos la leche fresca directamente de la finca de cada proveedor, todos los días, por turnos.
    </p>
</div>
<!-- Card Calidad -->
<div class="relative p-md rounded-xl bg-secondary overflow-hidden hover:shadow-2xl hover:-translate-y-2 transition-all reveal group"
     style="transition-delay: 100ms;">
    <div class="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center mb-md pulse-soft group-hover:scale-110 transition-transform">
        <span class="material-symbols-outlined text-white text-2xl">science</span>
    </div>
    <h3 class="font-headline-md text-headline-md text-white mb-sm">Calidad</h3>
    <p class="font-body-md text-body-md text-white/90">
        Cada recojo pasa por control de laboratorio antes de entrar a producción.
    </p>
</div>
<!-- Card Producción -->
<div class="relative p-md rounded-xl bg-tertiary overflow-hidden hover:shadow-2xl hover:-translate-y-2 transition-all reveal group"
     style="transition-delay: 200ms;">
    <div class="w-12 h-12 rounded-full bg-white/25 flex items-center justify-center mb-md pulse-soft group-hover:scale-110 transition-transform">
        <span class="material-symbols-outlined text-on-tertiary text-2xl">skillet</span>
    </div>
    <h3 class="font-headline-md text-headline-md text-on-tertiary mb-sm">Producción</h3>
    <p class="font-body-md text-body-md text-on-tertiary/90">
        Transformamos la leche en queso, yogurt y otros derivados con recetas propias.
    </p>
</div>
<!-- Card Ventas -->
<div class="relative p-md rounded-xl bg-error overflow-hidden hover:shadow-2xl hover:-translate-y-2 transition-all reveal group"
     style="transition-delay: 300ms;">
    <div class="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center mb-md pulse-soft group-hover:scale-110 transition-transform">
        <span class="material-symbols-outlined text-white text-2xl">storefront</span>
    </div>
    <h3 class="font-headline-md text-headline-md text-white mb-sm">Ventas</h3>
    <p class="font-body-md text-body-md text-white/90">
        Vendemos nuestros productos directamente a la comunidad, con boleta al instante.
    </p>
</div>
</div>
</section>

<!-- CONTÁCTANOS Section -->
<section class="pad-y pad-x bg-surface-container-low/50" id="contacto">
    <div class="container-max">
        <div class="text-center mb-12 sm:mb-xl reveal">
            <h2 class="font-display-lg fluid-headline-lg text-on-background mb-sm">CONTÁCTANOS</h2>
            <p class="font-body-md fluid-body text-on-surface-variant">Escríbenos si eres productor de la zona y quieres asociarte, o si tienes alguna consulta.</p>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 sm:gap-md">
            <!-- Email Card -->
            <div class="bg-background/80 backdrop-blur-sm p-6 sm:p-lg rounded-2xl flex flex-col items-center text-center shadow-sm hover:shadow-xl transition-all duration-300 reveal group sm:col-span-2 md:col-span-1">
                <div class="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-primary-container flex items-center justify-center mb-md transition-transform group-hover:rotate-12">
                    <span class="material-symbols-outlined text-on-primary-container text-2xl">mail</span>
                </div>
                <h4 class="font-headline-md fluid-headline-md text-on-background mb-xs">Correo</h4>
                <p class="font-body-md fluid-body text-on-surface-variant break-all">contacto@ecolacteoshuata.pe</p>
            </div>

            <!-- Phone Card -->
            <div class="bg-background/80 backdrop-blur-sm p-6 sm:p-lg rounded-2xl flex flex-col items-center text-center shadow-sm hover:shadow-xl transition-all duration-300 reveal group" style="transition-delay: 100ms;">
                <div class="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-secondary-container flex items-center justify-center mb-md transition-transform group-hover:rotate-12">
                    <span class="material-symbols-outlined text-on-secondary-container text-2xl">call</span>
                </div>
                <h4 class="font-headline-md fluid-headline-md text-on-background mb-xs">Teléfono</h4>
                <p class="font-body-md fluid-body text-on-surface-variant">{{ $empresa->telefono ?: 'Por confirmar' }}</p>
            </div>

            <!-- Address Card -->
            <div class="bg-background/80 backdrop-blur-sm p-6 sm:p-lg rounded-2xl flex flex-col items-center text-center shadow-sm hover:shadow-xl transition-all duration-300 reveal group" style="transition-delay: 200ms;">
                <div class="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-tertiary-container flex items-center justify-center mb-md transition-transform group-hover:rotate-12">
                    <span class="material-symbols-outlined text-on-tertiary-container text-2xl">location_on</span>
                </div>
                <h4 class="font-headline-md fluid-headline-md text-on-background mb-xs">Ubicación</h4>
                <p class="font-body-md fluid-body text-on-surface-variant">{{ $empresa->direccion ?: 'Huata, Puno, Perú' }}</p>
            </div>
        </div>

        <!-- Map -->
        <div class="mt-10 sm:mt-xl rounded-2xl overflow-hidden h-64 sm:h-80 md:h-96 shadow-lg reveal">
            <div class="w-full h-full bg-surface-container-high relative">
                <iframe
                    class="w-full h-full block"
                    src="https://maps.google.com/maps?q=Huata%2C%20Puno%2C%20Peru&t=&z=12&ie=UTF8&iwloc=&output=embed"
                    loading="lazy"
                    referrerpolicy="no-referrer-when-downgrade"
                    title="Ubicación Ecolácteos Huata">
                </iframe>
                <div class="absolute top-3 sm:top-4 left-1/2 -translate-x-1/2 pointer-events-none">
                    <div class="bg-background/90 backdrop-blur-sm px-3 sm:px-md py-2 sm:py-sm rounded-lg shadow-xl font-label-md text-xs sm:text-sm border border-primary/20 whitespace-nowrap">
                        Ecolácteos Huata — Huata, Puno
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Footer -->
<footer class="bg-surface-container border-t border-primary/10">
    <div class="pad-x container-max flex flex-col md:flex-row justify-between items-center gap-8 md:gap-6"
         style="padding-top: clamp(40px, 6vw, 64px); padding-bottom: clamp(40px, 6vw, 64px);">
        <div class="flex flex-col items-center md:items-start text-center md:text-left">
            <span class="font-display-lg text-headline-md text-on-surface mb-xs">Ecolácteos Huata</span>
            <p class="text-on-surface-variant font-body-md max-w-xs">Leche de Huata, cuidada desde la finca hasta tu mesa.</p>
        </div>
        <div class="flex flex-col items-center md:items-end gap-md w-full md:w-auto">
            <p class="text-on-surface-variant font-body-md text-sm text-center md:text-right">&copy; {{ now()->year }} Ecolácteos Huata. Todos los derechos reservados.</p>
            <a href="{{ route('login') }}" class="px-7 py-3.5 rounded-full bg-primary text-on-primary font-bold font-label-md text-base shadow-lg shadow-primary/40 ring-2 ring-primary/20 hover:scale-105 hover:shadow-xl hover:shadow-primary/50 transition-all">
                Iniciar sesión
            </a>
        </div>
    </div>
</footer>

<script>
        // Intersection Observer for scroll reveals
        const revealElements = document.querySelectorAll('.reveal');
        const observerOptions = {
            threshold: 0.15,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('active');
                }
            });
        }, observerOptions);

        revealElements.forEach(el => observer.observe(el));

        // Navbar shrinking on scroll
        window.addEventListener('scroll', () => {
            const nav = document.querySelector('nav');
            if (window.scrollY > 80) {
                nav.classList.add('glass-nav-scrolled');
            } else {
                nav.classList.remove('glass-nav-scrolled');
            }
        });

        // Menú móvil: abrir / cerrar
        const mobileMenuBtn = document.getElementById('mobile-menu-btn');
        const mobileMenu    = document.getElementById('mobile-menu');
        const mainNav       = document.getElementById('main-nav');

        function setMobileMenuOpen(open) {
            if (!mobileMenuBtn || !mobileMenu || !mainNav) return;
            mobileMenuBtn.classList.toggle('is-open', open);
            mobileMenu.classList.toggle('is-open', open);
            mainNav.classList.toggle('nav-open', open);
            mobileMenuBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
            mobileMenuBtn.setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú');
        }

        mobileMenuBtn?.addEventListener('click', () => {
            setMobileMenuOpen(!mobileMenu.classList.contains('is-open'));
        });
        mobileMenu?.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => setMobileMenuOpen(false));
        });
        window.addEventListener('resize', () => {
            if (window.innerWidth >= 768) setMobileMenuOpen(false);
        });

        // Resaltado de la sección activa en navegación
        const sections = document.querySelectorAll('section');
        const navLinks = document.querySelectorAll('.nav-section-link');

        window.addEventListener('scroll', () => {
            let current = "";
            sections.forEach(section => {
                const sectionTop = section.offsetTop;
                if (window.scrollY >= (sectionTop - 200)) {
                    current = section.getAttribute('id');
                }
            });

            navLinks.forEach(link => {
                const inMobileMenu = !!link.closest('#mobile-menu');
                const isActive = link.getAttribute('href').substring(1) === current;

                link.classList.remove(
                    'text-primary', 'font-bold', 'font-semibold',
                    'border-b-2', 'border-l-4', 'border-primary', 'pb-1',
                    'text-on-surface-variant'
                );

                if (isActive) {
                    link.classList.add('text-primary', 'font-bold');
                    if (inMobileMenu) {
                        link.classList.add('border-l-4', 'border-primary', 'pl-3');
                    } else {
                        link.classList.add('border-b-2', 'border-primary', 'pb-1');
                    }
                } else {
                    link.classList.add('text-on-surface-variant');
                    if (inMobileMenu) {
                        link.classList.add('font-semibold', 'pl-3');
                    }
                }
            });
        });
    </script>
</body>
</html>
