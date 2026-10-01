import { useEffect, useRef, useState } from "react";
export default function MediaSlot({
  src,
  poster,
  alt = "",
  className = "",
}: {
  src?: string;
  poster?: string;
  alt?: string;
  className?: string;
}) {
  const ref = useRef<HTMLVideoElement>(null);
  const [available, setAvailable] = useState(false);
  const [motionAllowed, setMotionAllowed] = useState(false);
  useEffect(() => {
    if (!src) return;
    let cancelled = false;
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const saveData = "connection" in navigator && Boolean((navigator as Navigator & { connection?: { saveData?: boolean } }).connection?.saveData);
    setMotionAllowed(!reduced && !saveData);
    fetch(src, { method: "HEAD" })
      .then((response) => { if (!cancelled) setAvailable(response.ok); })
      .catch(() => { if (!cancelled) setAvailable(false); });
    return () => { cancelled = true; };
  }, [src]);
  useEffect(() => {
    const video = ref.current;
    if (!video || !src || !available || !motionAllowed) return;
    const observer = new IntersectionObserver(
      ([entry]) =>
        entry.isIntersecting ? video.play().catch(() => {}) : video.pause(),
      { threshold: 0.1 },
    );
    observer.observe(video);
    return () => observer.disconnect();
  }, [src, available, motionAllowed]);
  if (!src || !available)
    return (
      <div
        className={`media-slot media-fallback ${className}`}
        aria-label={alt}
      >
        <div className="media-scrim" />
        <span className="media-sun" aria-hidden="true" />
      </div>
    );
  return (
    <video
      ref={ref}
      className={`media-slot ${className}`}
      src={src}
      poster={poster}
      muted
      loop
      playsInline
      autoPlay={motionAllowed}
      preload="metadata"
      aria-hidden={!alt}
    >
      {alt && <track kind="captions" />}
    </video>
  );
}
