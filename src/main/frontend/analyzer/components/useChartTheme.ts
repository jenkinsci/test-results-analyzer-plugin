import { type RefObject, useEffect, useState } from "react";

import { type ChartTheme, readTheme } from "../utils/charts.ts";

/**
 * The colours of the Jenkins theme for charts drawn in the element, read again whenever the
 * theme changes.
 */
export function useChartTheme(
  element: RefObject<HTMLElement | null>,
): ChartTheme | null {
  const [theme, setTheme] = useState<ChartTheme | null>(null);

  useEffect(() => {
    const update = () => {
      if (element.current) {
        setTheme(readTheme(element.current));
      }
    };
    update();

    let frame = 0;
    const onChange = () => {
      // Wait a frame so the new theme's stylesheet has been applied before reading tokens
      cancelAnimationFrame(frame);
      frame = requestAnimationFrame(update);
    };
    const observer = new MutationObserver(onChange);
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ["data-theme", "class", "style"],
    });
    observer.observe(document.body, {
      attributes: true,
      attributeFilter: ["data-theme", "class"],
    });
    const query = window.matchMedia?.("(prefers-color-scheme: dark)");
    query?.addEventListener("change", onChange);
    return () => {
      cancelAnimationFrame(frame);
      observer.disconnect();
      query?.removeEventListener("change", onChange);
    };
  }, [element]);

  return theme;
}
