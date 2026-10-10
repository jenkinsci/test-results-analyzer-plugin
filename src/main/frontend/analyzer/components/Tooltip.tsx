import Tippy, { type TippyProps } from "@tippyjs/react";

/** Tippy with Jenkins core's tooltip theme, so tooltips match the rest of the UI. */
export function Tooltip(props: TippyProps) {
  return (
    <Tippy
      theme="tooltip"
      animation="tooltip"
      duration={250}
      touch={false}
      delay={[200, 0]}
      {...props}
    >
      {props.children}
    </Tippy>
  );
}
