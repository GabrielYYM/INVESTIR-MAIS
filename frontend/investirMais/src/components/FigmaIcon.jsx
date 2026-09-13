export function FigmaIcon({ src, outer, leaf, alt = '', className = '' }) {
    const [outerW, outerH] = outer;
    const [leafW, leafH] = leaf;
    const padX = Math.max(0, (outerW - leafW) / 2);
    const padY = Math.max(0, (outerH - leafH) / 2);

    return (
        <span
            className={`inline-flex shrink-0 items-center justify-center overflow-visible ${className}`}
            style={{ width: outerW, height: outerH, padding: `${padY}px ${padX}px` }}
        >
            <img
                src={src}
                alt={alt}
                width={leafW}
                height={leafH}
                className="block max-w-none"
                style={{ width: leafW, height: leafH }}
            />
        </span>
    );
}
