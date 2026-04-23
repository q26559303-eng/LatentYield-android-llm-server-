import { useState } from 'react';
import { Moon, Sun } from 'lucide-react';
import { useServerMetrics } from '../hooks/useServerMetrics';
import { useServerLogs } from '../hooks/useServerLogs';

type Language = 'zh' | 'en';
type Theme = 'dark' | 'light';

const translations = {
  zh: {
    title: '灵源',
    model: '当前模型',
    status: '服务状态',
    running: '运行中',
    switchModel: '切换模型',
    memoryLoad: '内存负载 (6GB)',
    computeLoad: '计算负载 (天玑810)',
    console: '运行日志',
    copy: '复制',
    copied: '已复制!',
    clear: '清空',
  },
  en: {
    title: 'LatentYield',
    model: 'Current Model',
    status: 'Status',
    running: 'Running',
    switchModel: 'Switch Model',
    memoryLoad: 'Memory Load (6GB)',
    computeLoad: 'Compute Load (Dimensity 810)',
    console: 'Console Log',
    copy: 'Copy',
    copied: 'Copied!',
    clear: 'Clear',
  },
};

export default function App() {
  const [lang, setLang] = useState<Language>('zh');
  const [theme, setTheme] = useState<Theme>('dark');
  const [isRunning, setIsRunning] = useState(false);
  
  const { cpuUsage, temperature, tokenSpeed } = useServerMetrics();
  const { logs, copied, handleCopy, handleClear } = useServerLogs();

  // 监听 Android 进程退出通知
  useState(() => {
    (window as any).onAndroidExit = () => {
      setIsRunning(false);
    };
  });

  const handleToggleServer = () => {
    if (isRunning) {
      if ((window as any).Android) {
        (window as any).Android.stopServer();
      }
      setIsRunning(false);
    } else {
      if ((window as any).Android) {
        (window as any).Android.startServer();
      }
      setIsRunning(true);
    }
  };

  const t = translations[lang];

  const isDark = theme === 'dark';

  return (
    <div
      className={`min-h-screen ${
        isDark
          ? 'bg-[#1a1a1a]'
          : 'bg-gradient-to-br from-cyan-400 via-blue-400 to-blue-500'
      } transition-colors duration-500`}
    >
        {/* Header */}
        <header className={`px-5 py-4 ${isDark ? '' : ''}`}>
          <div className="flex items-center justify-between">
            <h1 className={`text-2xl font-[700] ${
              isDark ? 'text-white' : 'text-white'
            }`}>
              {t.title}
            </h1>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setLang(lang === 'zh' ? 'en' : 'zh')}
                className={`px-3 py-1.5 rounded-xl text-sm font-[600] transition-all ${
                  isDark
                    ? 'bg-[#2a2a2a] hover:bg-[#333] text-cyan-400'
                    : 'bg-white/20 hover:bg-white/30 text-white backdrop-blur-sm'
                }`}
              >
                {lang === 'zh' ? '中' : 'EN'}
              </button>

              <button
                onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
                className={`p-2 rounded-xl transition-all ${
                  isDark
                    ? 'bg-[#2a2a2a] hover:bg-[#333] text-purple-400'
                    : 'bg-white/20 hover:bg-white/30 text-white backdrop-blur-sm'
                }`}
              >
                {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </header>

      <main className="px-5 space-y-5 pb-6">
        {/* Model Card */}
        <div
          className={`rounded-[28px] p-5 ${
            isDark
              ? 'bg-[#252525]'
              : 'bg-[#2a2a2a]/90 backdrop-blur-sm'
          }`}
        >
          <div className="mb-5">
            <div className={`text-xs uppercase tracking-wider mb-1 ${
              isDark ? 'text-gray-500' : 'text-gray-400'
            }`}>
              {t.model}
            </div>
            <div className={`text-lg font-[600] ${
              isDark ? 'text-white' : 'text-white'
            }`}>
              Gemma-3-1b-it
            </div>
            <div className={`flex gap-3 mt-2 text-xs ${
              isDark ? 'text-gray-400' : 'text-gray-300'
            }`}>
              <span className="font-mono">Q4_K_M</span>
              <span className="font-mono">CPU (2 Threads)</span>
              <span className="font-mono">CTX: 2048</span>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className={`w-2 h-2 rounded-full animate-pulse ${
                isDark ? 'bg-cyan-400' : 'bg-cyan-400'
              }`} />
              <span className={`text-sm ${
                isDark ? 'text-cyan-400' : 'text-cyan-300'
              }`}>
                {t.status}: {isRunning ? t.running : "已停止"}
              </span>
            </div>

            <button
              onClick={handleToggleServer}
              className={`px-5 py-2.5 rounded-2xl text-sm font-[600] transition-all ${
                isRunning
                  ? 'bg-red-500 hover:bg-red-600 text-white'
                  : 'bg-gradient-to-r from-purple-600 to-purple-500 hover:from-purple-500 hover:to-purple-400 text-white'
              }`}
            >
              {isRunning ? "停止服务" : "启动引擎"}
            </button>
          </div>
        </div>

        {/* Resources Card */}
        <div
          className={`rounded-[28px] p-5 ${
            isDark
              ? 'bg-[#252525]'
              : 'bg-[#2a2a2a]/90 backdrop-blur-sm'
          }`}
        >
          {/* Memory Load */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-2">
              <div className={`text-sm font-[500] ${isDark ? 'text-gray-300' : 'text-gray-200'}`}>
                {t.memoryLoad}
              </div>
              <div className={`text-sm font-mono font-[600] ${
                isDark ? 'text-cyan-400' : 'text-cyan-300'
              }`}>
                1.4 / 6.0 GB
              </div>
            </div>
            <div className={`h-2 rounded-full overflow-hidden ${
              isDark ? 'bg-[#1a1a1a]' : 'bg-black/30'
            }`}>
              <div
                className="h-full rounded-full transition-all duration-500 bg-gradient-to-r from-cyan-500 to-cyan-400"
                style={{ width: '23.3%' }}
              />
            </div>
          </div>

          {/* Compute Load */}
          <div>
            <div className={`text-sm font-[500] mb-4 ${isDark ? 'text-gray-300' : 'text-gray-200'}`}>
              {t.computeLoad}
            </div>
            <div className="flex gap-5">
              {/* GPU Usage Circle */}
              <div className="flex flex-col items-center">
                <div className="relative w-24 h-24">
                  <svg className="w-full h-full -rotate-90">
                    <circle
                      cx="48"
                      cy="48"
                      r="38"
                      stroke={isDark ? '#1a1a1a' : '#1a1a1a'}
                      strokeWidth="8"
                      fill="none"
                    />
                      <circle
                      cx="48"
                      cy="48"
                      r="38"
                      stroke="url(#gradient-purple)"
                      strokeWidth="8"
                      fill="none"
                      strokeDasharray={`${2 * Math.PI * 38}`}
                      strokeDashoffset={`${2 * Math.PI * 38 * (1 - cpuUsage / 100)}`}
                      strokeLinecap="round"
                      className="transition-all duration-500"
                    />
                    <defs>
                      <linearGradient id="gradient-purple" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" stopColor="#a855f7" />
                        <stop offset="100%" stopColor="#8b5cf6" />
                      </linearGradient>
                    </defs>
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="text-center">
                      <div className="text-xl font-[700] text-white">
                        {Math.round(cpuUsage)}%
                      </div>
                    </div>
                  </div>
                </div>
                <div className={`text-xs mt-1.5 ${isDark ? 'text-gray-500' : 'text-gray-400'}`}>
                  CPU (A76)
                </div>
                <div className={`text-xs font-mono ${isDark ? 'text-gray-400' : 'text-gray-300'}`}>
                  {Math.round(temperature)}°C
                </div>
              </div>

              {/* Token Speed */}
              <div className="flex-1 flex flex-col justify-center">
                <div className={`text-xs uppercase tracking-wider mb-1 ${
                  isDark ? 'text-gray-500' : 'text-gray-400'
                }`}>
                  {lang === 'zh' ? '生成速度' : 'Gen Speed'}
                </div>
                <div className="text-4xl font-[700] font-mono text-white">
                  {tokenSpeed.toFixed(1)}
                </div>
                <div className={`text-xs mt-1 ${isDark ? 'text-gray-500' : 'text-gray-400'}`}>
                  TOKEN/S
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Console Card - Windows CMD Style */}
        <div
          className={`rounded-[28px] overflow-hidden ${
            isDark
              ? 'bg-[#252525]'
              : 'bg-[#2a2a2a]/90 backdrop-blur-sm'
          }`}
        >
          {/* Windows Title Bar */}
          <div className="flex items-center justify-between px-4 py-2 bg-[#1a1a1a]">
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 bg-[#C0C0C0] rounded-sm flex items-center justify-center">
                <div className="w-2.5 h-2.5 bg-black rounded-sm" />
              </div>
              <div className="text-[11px] font-[500] text-gray-400 tracking-wide">
                {t.console} - /data/app/com.localllm.server
              </div>
            </div>
            
            <div className="flex gap-2">
              <button 
                onClick={handleClear}
                className="text-[10px] px-2 py-0.5 rounded bg-gray-700 hover:bg-gray-600 text-white transition-colors"
              >
                {t.clear}
              </button>
              <button 
                onClick={handleCopy}
                className="text-[10px] px-2 py-0.5 rounded bg-purple-600 hover:bg-purple-500 text-white transition-colors"
              >
                {copied ? t.copied : t.copy}
              </button>
            </div>
          </div>

          {/* Console Content */}
          <div className="p-4 bg-black font-mono text-[11px] leading-relaxed min-h-[200px]">
            <div className="space-y-0.5">
              {logs.map((log, index) => (
                <div key={index} className={
                  log.startsWith('✅') ? 'text-lime-400' : 
                  log.startsWith('🤖') || log.startsWith('┌') || log.startsWith('│') || log.startsWith('└') ? 'text-purple-300' : 
                  log.startsWith('▶') ? 'text-cyan-400' : 
                  log.startsWith('⏳') ? 'text-yellow-300' : 'text-gray-300'
                }>
                  {log}
                </div>
              ))}
              <div className="flex items-center mt-1">
                <span className="text-cyan-400">127.0.0.1:8080&gt;</span>
                <div className="inline-block w-2 h-3.5 ml-0.5 bg-gray-300 animate-pulse" />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
