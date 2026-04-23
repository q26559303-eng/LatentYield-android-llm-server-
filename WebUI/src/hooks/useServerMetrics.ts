import { useState, useEffect } from 'react';

export function useServerMetrics() {
  const [cpuUsage, setCpuUsage] = useState(42);
  const [temperature, setTemperature] = useState(45);
  const [tokenSpeed, setTokenSpeed] = useState(8.5);

  useEffect(() => {
    const interval = setInterval(() => {
      setCpuUsage((prev) => Math.min(100, Math.max(0, prev + (Math.random() - 0.5) * 10)));
      setTemperature((prev) => Math.min(75, Math.max(35, prev + (Math.random() - 0.5) * 5)));
      setTokenSpeed((prev) => Math.max(0, prev + (Math.random() - 0.5) * 2));
    }, 2000);
    return () => clearInterval(interval);
  }, []);

  return { cpuUsage, temperature, tokenSpeed };
}
