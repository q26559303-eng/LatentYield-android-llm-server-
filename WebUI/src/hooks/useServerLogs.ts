import { useState, useEffect } from 'react';

export function useServerLogs() {
  const [logs, setLogs] = useState<string[]>(["等待底层 Android 引擎启动..."]);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    (window as any).onAndroidLog = (msg: string) => {
      setLogs((prev) => {
        const newLogs = [...prev, msg];
        return newLogs.length > 200 ? newLogs.slice(newLogs.length - 200) : newLogs;
      });
    };
    return () => {
      delete (window as any).onAndroidLog;
    };
  }, []);

  const handleCopy = () => {
    navigator.clipboard.writeText(logs.join('\n'));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleClear = () => {
    setLogs(["日志已清空..."]);
  };

  return { logs, copied, handleCopy, handleClear };
}
