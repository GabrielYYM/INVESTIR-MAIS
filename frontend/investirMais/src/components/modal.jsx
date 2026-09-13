export function Modal({ isOpen, onClose, title, children, maxWidth = 'max-w-lg' }) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
            <div className={`relative w-full ${maxWidth} rounded-[46px] border border-white/10 bg-[#0d0d0d] p-6 shadow-2xl`}>
                {onClose && (
                    <button
                        onClick={onClose}
                        className="absolute top-4 right-4 text-gray-400 transition-colors hover:text-white"
                        aria-label="Fechar"
                    >
                        ✕
                    </button>
                )}

                {title && (
                    <h2 className="mb-6 font-['Poppins'] text-2xl font-semibold text-white">
                        {title}
                    </h2>
                )}

                {children}
            </div>
        </div>
    );
}
