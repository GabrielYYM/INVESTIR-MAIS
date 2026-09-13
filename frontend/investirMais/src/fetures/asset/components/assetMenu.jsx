import { useState, useRef, useEffect } from 'react';
import { figmaAssets } from '../../../assets/figma';
import { FigmaIcon } from '../../../components/FigmaIcon';

export function AssetMenu({ assetId, onEdit, onDelete, onEvaluate }) {
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef(null);

    useEffect(() => {
        function handleClickOutside(event) {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div className="relative inline-flex" ref={menuRef}>
            <button
                type="button"
                onClick={() => setIsOpen(!isOpen)}
                className="rounded-full focus:outline-none"
                aria-label="Abrir menu do ativo"
            >
                <FigmaIcon src={figmaAssets.iconKebabRow} outer={[32, 32]} leaf={[32, 32]} />
            </button>

            {isOpen && (
                <div className="absolute right-0 z-20 mt-2 w-48 overflow-hidden rounded-lg border border-white/10 bg-[#1f1d2b] py-1 shadow-xl">
                    <button
                        type="button"
                        onClick={() => { onEvaluate(assetId); setIsOpen(false); }}
                        className="block w-full px-4 py-2 text-left text-sm font-medium text-[#e29f26] hover:bg-white/5"
                    >
                        Avaliar
                    </button>
                    <button
                        type="button"
                        onClick={() => { onEdit(assetId); setIsOpen(false); }}
                        className="block w-full px-4 py-2 text-left text-sm text-gray-200 hover:bg-white/5"
                    >
                        Editar
                    </button>
                    <button
                        type="button"
                        onClick={() => { onDelete(assetId); setIsOpen(false); }}
                        className="block w-full px-4 py-2 text-left text-sm text-red-400 hover:bg-white/5"
                    >
                        Deletar
                    </button>
                </div>
            )}
        </div>
    );
}
