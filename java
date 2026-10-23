const COLOR_PALETTE = [
  { name: 'Vermelho', hex: '#ff5252' },
  { name: 'Azul', hex: '#448aff' },
  { name: 'Amarelo', hex: '#ffd700' },
  { name: 'Verde', hex: '#66bb6a' },
  { name: 'Roxo', hex: '#ab47bc' },
  { name: 'Laranja', hex: '#ff9800' },
  { name: 'Rosa', hex: '#ff4081' }
];

const ENCOURAGING_MESSAGES = [
  "Muito Bem! 🎉", "Excelente! ⭐", "Mandou Bem! 🎈", 
  "Show! 🌟", "Uhuul! 🚀", "Parabéns! 👏", "Fantástico! ✨"
];

let score = 0;
let soundEnabled = true;
let currentMode = 'free'; // 'free' ou 'color'
let targetColor = null;
let spawnInterval = null;

// Elementos de Interface
const mainMenu = document.getElementById('mainMenu');
const creditsModal = document.getElementById('creditsModal');
const gameHeader = document.getElementById('gameHeader');
const targetBar = document.getElementById('targetBar');
const canvas = document.getElementById('gameCanvas');
const gameFooter = document.getElementById('gameFooter');

const scoreDisplay = document.getElementById('scoreCounter');
const targetColorName = document.getElementById('targetColorName');
const targetBadge = document.getElementById('targetBadge');

// Botões do Menu e Header
const btnStartFree = document.getElementById('btnStartFree');
const btnStartColor = document.getElementById('btnStartColor');
const btnOpenCredits = document.getElementById('btnOpenCredits');
const btnCloseCredits = document.getElementById('btnCloseCredits');
const btnBackMenu = document.getElementById('btnBackMenu');
const btnSound = document.getElementById('btnSound');
const btnSpeakTarget = document.getElementById('btnSpeakTarget');

// Configuração de Som (Web Audio API)
const AudioContext = window.AudioContext || window.webkitAudioContext;
let audioCtx = null;

function initAudio() {
  if (!audioCtx) {
    audioCtx = new AudioContext();
  }
}

function playPopSound() {
  if (!soundEnabled) return;
  initAudio();
  if (audioCtx.state === 'suspended') audioCtx.resume();

  const osc = audioCtx.createOscillator();
  const gain = audioCtx.createGain();

  osc.connect(gain);
  gain.connect(audioCtx.destination);

  osc.type = 'triangle';
  osc.frequency.setValueAtTime(180, audioCtx.currentTime);
  osc.frequency.exponentialRampToValueAtTime(50, audioCtx.currentTime + 0.15);
  gain.gain.setValueAtTime(0.5, audioCtx.currentTime);
  gain.gain.linearRampToValueAtTime(0.01, audioCtx.currentTime + 0.15);
  
  osc.start();
  osc.stop(audioCtx.currentTime + 0.15);
}

function speakText(text) {
  if (!soundEnabled || !('speechSynthesis' in window)) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'pt-BR';
  utterance.rate = 0.9;
  window.speechSynthesis.speak(utterance);
}

// Mostrar Mensagem Motivacional Flutuante
function showFloatingMessage(x, y) {
  const msgText = ENCOURAGING_MESSAGES[Math.floor(Math.random() * ENCOURAGING_MESSAGES.length)];
  const msgEl = document.createElement('div');
  msgEl.className = 'popup-msg';
  msgEl.innerText = msgText;
  msgEl.style.left = `${x}px`;
  msgEl.style.top = `${y}px`;
  canvas.appendChild(msgEl);
  setTimeout(() => msgEl.remove(), 800);
}

function setNextTargetColor() {
  const randomObj = COLOR_PALETTE[Math.floor(Math.random() * COLOR_PALETTE.length)];
  targetColor = randomObj;
  targetColorName.innerText = targetColor.name;
  targetBadge.style.backgroundColor = targetColor.hex;
  speakText(`Estoure a cor ${targetColor.name}`);
}

// Criar Balão na Tela
function spawnItem() {
  const item = document.createElement('div');
  item.className = 'pop-item';

  let colorObj;
  if (currentMode === 'color' && Math.random() < 0.5 && targetColor) {
    colorObj = targetColor;
  } else {
    colorObj = COLOR_PALETTE[Math.floor(Math.random() * COLOR_PALETTE.length)];
  }

  item.dataset.colorName = colorObj.name;

  const shape = document.createElement('div');
  shape.className = 'balloon-shape';
  shape.style.backgroundColor = colorObj.hex;
  shape.style.color = colorObj.hex;

  const string = document.createElement('div');
  string.className = 'balloon-string';

  item.appendChild(shape);
  item.appendChild(string);

  const startX = Math.random() * (window.innerWidth - 100) + 50;
  item.style.left = `${startX}px`;
  item.style.bottom = `-130px`;

  canvas.appendChild(item);

  let posY = -130;
  const speed = 1.3 + Math.random() * 1.5;

  function moveUp() {
    if (!item.parentElement) return;
    posY += speed;
    item.style.bottom = `${posY}px`;

    if (posY > window.innerHeight + 120) {
      item.remove();
    } else {
      requestAnimationFrame(moveUp);
    }
  }

  requestAnimationFrame(moveUp);

  // Ação ao Estourar
  function popAction(e) {
    e.stopPropagation();
    
    item.classList.add('popping');
    playPopSound();

    const rect = item.getBoundingClientRect();
    showFloatingMessage(rect.left + rect.width / 2, rect.top + rect.height / 2);

    if (currentMode === 'color') {
      if (item.dataset.colorName === targetColor.name) {
        score += 1;
        scoreDisplay.innerText = score;
        setNextTargetColor();
      } else {
        speakText(`Esse é ${item.dataset.colorName}`);
      }
    } else {
      score += 1;
      scoreDisplay.innerText = score;
    }

    setTimeout(() => item.remove(), 300);
  }

  item.addEventListener('pointerdown', popAction);
}

function startGame(mode) {
  currentMode = mode;
  score = 0;
  scoreDisplay.innerText = score;

  mainMenu.classList.add('hidden');
  gameHeader.classList.remove('hidden');
  canvas.classList.remove('hidden');
  gameFooter.classList.remove('hidden');

  if (currentMode === 'color') {
    targetBar.classList.remove('hidden');
    setNextTargetColor();
  } else {
    targetBar.classList.add('hidden');
    speakText("Modo livre. Divirta-se estourando os balões!");
  }

  clearInterval(spawnInterval);
  spawnInterval = setInterval(spawnItem, 1400);
}

function returnToMenu() {
  clearInterval(spawnInterval);
  canvas.innerHTML = '';
  window.speechSynthesis.cancel();

  gameHeader.classList.add('hidden');
  targetBar.classList.add('hidden');
  canvas.classList.add('hidden');
  gameFooter.classList.add('hidden');
  mainMenu.classList.remove('hidden');
}

// Eventos dos Botões
btnStartFree.addEventListener('click', () => startGame('free'));
btnStartColor.addEventListener('click', () => startGame('color'));

btnOpenCredits.addEventListener('click', () => creditsModal.classList.remove('hidden'));
btnCloseCredits.addEventListener('click', () => creditsModal.classList.add('hidden'));

btnBackMenu.addEventListener('click', returnToMenu);

btnSound.addEventListener('click', () => {
  soundEnabled = !soundEnabled;
  btnSound.innerText = soundEnabled ? '🔊' : '🔇';
});

btnSpeakTarget.addEventListener('click', () => {
  if (targetColor) {
    speakText(`Estoure a cor ${targetColor.name}`);
  }
});
