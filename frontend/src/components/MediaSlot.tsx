import { useEffect, useRef } from "react";
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
  useEffect(() => {
    const video = ref.current;
    if (!video || !src) return;
    const observer = new IntersectionObserver(
      ([entry]) =>
        entry.isIntersecting ? video.play().catch(() => {}) : video.pause(),
      { threshold: 0.1 },
    );
    observer.observe(video);
    return () => observer.disconnect();
  }, [src]);
  if (!src)
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
      preload="metadata"
      aria-hidden={!alt}
    >
      {alt && <track kind="captions" />}
    </video>
  );
}
