import type { ECharts, EChartsOption } from "echarts";
import { useEffect, useRef, useState } from "react";

import type { ChartTheme } from "../utils/charts.ts";
import { useChartTheme } from "./useChartTheme.ts";

/** Below this width the pie slice labels do not fit. */
const WIDE_CHART = 420;

interface EChartProps {
  /** Builds the chart options; wide says whether there is room for slice labels. */
  buildOption: (theme: ChartTheme, wide: boolean) => EChartsOption;
  /** Called with the category index when a column of the chart's grid is clicked. */
  onColumnClick?: (index: number) => void;
}

/**
 * An ECharts chart from the echarts-api plugin. It is resized with its container and redrawn
 * in the colours of the Jenkins theme whenever the theme changes.
 */
export function EChart({ buildOption, onColumnClick }: EChartProps) {
  const element = useRef<HTMLDivElement>(null);
  const [chart, setChart] = useState<ECharts | null>(null);
  const [wide, setWide] = useState(true);
  const theme = useChartTheme(element);

  useEffect(() => {
    const echarts = window.echarts;
    if (!echarts || !element.current) {
      return;
    }
    const instance = echarts.init(element.current, null, { renderer: "svg" });
    setChart(instance);
    setWide(instance.getWidth() >= WIDE_CHART);
    const observer =
      typeof ResizeObserver === "undefined"
        ? null
        : new ResizeObserver(() => {
            instance.resize();
            setWide(instance.getWidth() >= WIDE_CHART);
          });
    observer?.observe(element.current);
    return () => {
      observer?.disconnect();
      instance.dispose();
      setChart(null);
    };
  }, []);

  useEffect(() => {
    if (chart && theme) {
      chart.setOption(buildOption(theme, wide), true);
    }
  }, [chart, theme, buildOption, wide]);

  useEffect(() => {
    if (!chart || !onColumnClick) {
      return;
    }
    const zr = chart.getZr();
    const onClick = (event: { offsetX: number; offsetY: number }) => {
      const point = [event.offsetX, event.offsetY];
      if (!chart.containPixel("grid", point)) {
        return;
      }
      const [index] = chart.convertFromPixel(
        { seriesIndex: 0 },
        point,
      ) as number[];
      onColumnClick(index);
    };
    zr.on("click", onClick);
    return () => {
      zr.off("click", onClick);
    };
  }, [chart, onColumnClick]);

  return <div className="tra-chart" ref={element} />;
}
