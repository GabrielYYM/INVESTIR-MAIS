const badgeSizes = {
  sm: { box: "size-10 rounded-xl", iconSize: 20 },
  lg: { box: "size-16 rounded-2xl", iconSize: 30 },
  auth: { box: "mx-auto mb-4 size-16 rounded-full", iconSize: 32 },
};

export default function IconBadge({
  icon: Icon,
  size = "sm",
  className = "bg-amber-500/20 text-amber-400",
}) {
  const config = badgeSizes[size] || badgeSizes.sm;
  return (
    <span className={`grid shrink-0 place-items-center ${config.box} ${className}`}>
      {Icon && <Icon size={config.iconSize} />}
    </span>
  );
}
