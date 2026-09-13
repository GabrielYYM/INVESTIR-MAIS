import { figmaAssets } from '../assets/figma';
import { FigmaIcon } from './FigmaIcon';

export function Header() {
    return (
        <header className="flex items-center justify-between gap-6 mt-8 mb-2">
            <label className="relative block max-w-[625px] flex-1">
                <input
                    type="search"
                    placeholder="Pesquise"
                    className="h-10 w-full rounded-[10px] bg-white/10 px-4 pr-10 font-['Poppins'] text-[12px] tracking-[0.5px] text-white placeholder:text-[#808191] focus:outline-none"
                />
                <span className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2">
                    <FigmaIcon src={figmaAssets.iconSearch} outer={[12, 12]} leaf={[13.31, 13.31]} />
                </span>
            </label>

            <div className="flex items-center gap-3">
                <img
                    src={figmaAssets.avatar}
                    alt=""
                    width={32}
                    height={32}
                    className="size-8 rounded-full object-cover"
                />
                <p className="font-['Poppins'] text-[13px] tracking-[0.5px]">Usuário</p>
                <FigmaIcon src={figmaAssets.iconChevronDown} outer={[20, 20]} leaf={[20, 20]} />
                <span className="relative">
                    <FigmaIcon src={figmaAssets.iconNotification} outer={[20, 20]} leaf={[14.17, 16.67]} />
                    <span className="absolute -top-0.5 -right-0.5">
                        <FigmaIcon src={figmaAssets.iconNotificationDot} outer={[6, 6]} leaf={[6, 6]} />
                    </span>
                </span>
            </div>
        </header>
    );
}
