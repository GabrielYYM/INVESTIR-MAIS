const variants = {
  primary: "button-primary",
  secondary: "button-secondary",
  danger: "button-danger",
};

export function SubmitButton({
  loading = false,
  loadingText,
  disabled = false,
  icon: Icon,
  iconSize = 17,
  variant = "primary",
  fullWidth = false,
  type = "submit",
  className = "",
  children,
  ...props
}) {
  const baseClass = variants[variant] || variants.primary;
  return (
    <button
      type={type}
      disabled={loading || disabled}
      className={`${baseClass}${fullWidth ? " w-full" : ""}${className ? ` ${className}` : ""}`}
      {...props}
    >
      {Icon && <Icon size={iconSize} />}
      {loading && loadingText ? loadingText : children}
    </button>
  );
}

export { SubmitButton as LoadingButton };
export default SubmitButton;
