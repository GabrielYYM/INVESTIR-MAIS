import { NavLink } from 'react-router-dom';
import { figmaAssets } from '../assets/figma';
import { FigmaIcon } from './FigmaIcon';

function NavIconButton({ src, leaf, active }) {
    return (
        <span
            className={`inline-flex size-8 shrink-0 items-center justify-center rounded-[12px] ${
                active ? 'bg-[#e8cf40]' : 'bg-white/10'
            }`}
        >
            <FigmaIcon src={src} outer={[16, 16]} leaf={leaf} />
        </span>
    );
}

export function Sidebar() {
    return (
        <aside className="sticky top-0 flex h-full w-[220px] shrink-0 flex-col items-center justify-between bg-[#1f1d2b] px-6 pb-8">
            <div className="mt-8 flex h-10 items-center justify-center text-center">
                <p className="font-['Poppins'] text-[20px] font-semibold leading-5 tracking-wide">
                    INVESTIR MAIS
                </p>
            </div>

            <div className="my-auto flex flex-col items-center w-full">
                <p className="font-['Poppins'] text-[11px] font-semibold tracking-[1.5px] text-[#808191] opacity-50 w-full text-center">
                    MENU
                </p>

                <nav className="mt-[19px] flex flex-col gap-7 w-full items-center">
                    <span className="flex w-[140px] items-center gap-4 text-[14px] tracking-[0.3px] text-[#808191]">
                        <NavIconButton src={figmaAssets.iconHome} leaf={[12.67, 13.33]} />
                        <span className="font-['Poppins']">Home</span>
                    </span>

                    <NavLink
                        to="/portfolio"
                        className={({ isActive }) =>
                            `flex w-[140px] items-center gap-4 font-['Poppins'] text-[14px] tracking-[0.3px] ${
                                isActive ? 'font-semibold text-white' : 'text-[#808191]'
                            }`
                        }
                    >
                        {({ isActive }) => (
                            <>
                                <NavIconButton
                                    src={figmaAssets.iconWallet}
                                    leaf={[13.33, 12]}
                                    active={isActive}
                                />
                                Carteira
                            </>
                        )}
                    </NavLink>

                    <span className="flex w-[140px] items-center gap-4 text-[14px] leading-[15.5px] tracking-[0.3px] text-[#808191]">
                        <NavIconButton src={figmaAssets.iconDownload} leaf={[13.33, 12.67]} />
                        <span className="font-['Poppins']">
                            Orçamento
                            <br />
                            Doméstico
                        </span>
                    </span>

                    <span className="flex w-[140px] items-center gap-4 text-[14px] tracking-[0.3px] text-[#808191]">
                        <NavIconButton src={figmaAssets.iconPaperDownload} leaf={[11.33, 13.33]} />
                        <span className="font-['Poppins']">Ferramentas</span>
                    </span>

                    <span className="flex w-[140px] items-center gap-4 text-[14px] tracking-[0.3px] text-[#808191]">
                        <NavIconButton src={figmaAssets.iconWalletMuted} leaf={[13.33, 12]} />
                        <span className="font-['Poppins']">FAQ</span>
                    </span>

                    <NavLink
                        to="/questions"
                        className={({ isActive }) =>
                            `flex w-[140px] items-center gap-4 font-['Poppins'] text-[14px] tracking-[0.3px] ${
                                isActive ? 'font-semibold text-white' : 'text-[#808191]'
                            }`
                        }
                    >
                        {({ isActive }) => (
                            <>
                                <NavIconButton
                                    src={figmaAssets.iconQuestion}
                                    leaf={[14, 14]}
                                    active={isActive}
                                />
                                Perguntas
                            </>
                        )}
                    </NavLink>
                </nav>
            </div>

            <button
                type="button"
                className="flex w-[140px] items-center gap-4 text-left text-[14px] tracking-[0.3px] text-[#808191] hover:text-white transition-colors cursor-pointer"
            >
                <span className="inline-flex size-8 shrink-0 items-center justify-center rounded-[12px] bg-white/10">
                    <FigmaIcon src={figmaAssets.iconArrowRight} outer={[16, 16]} leaf={[12, 8]} />
                </span>
                <span className="font-['Poppins']">Sair</span>
            </button>
        </aside>
    );
}
