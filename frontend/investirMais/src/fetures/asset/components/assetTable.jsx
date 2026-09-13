import { AssetMenu } from './assetMenu';
import { figmaAssets } from '../../../assets/figma';
import { FigmaIcon } from '../../../components/FigmaIcon';

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const percent = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

export function AssetTable({ assets, onEdit, onDelete, onEvaluate }) {
    return (
        <div className="w-full rounded-[46px] bg-[#0d0d0d] px-12 py-10 shadow-xl">
            <div className="mb-8 flex items-center justify-between">
                <h2 className="font-['Poppins'] text-[24px] font-semibold tracking-[0.3px]">Lista de Ativos</h2>
                <div className="flex items-center gap-4">
                    <button type="button" className="cursor-pointer transition-opacity hover:opacity-75 focus:outline-none" title="Filtrar">
                        <FigmaIcon src={figmaAssets.iconFilter} outer={[32, 32]} leaf={[32, 32]} />
                    </button>
                    <button type="button" className="cursor-pointer transition-opacity hover:opacity-75 focus:outline-none" title="Ordenar">
                        <FigmaIcon src={figmaAssets.iconSort} outer={[32, 32]} leaf={[32, 32]} />
                    </button>
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full min-w-[650px] border-collapse text-left">
                    <thead>
                        <tr className="font-['Poppins'] text-[16px] tracking-[0.3px] text-[#b6b6b6]">
                            <th className="px-4 pb-8 font-normal">Tipo</th>
                            <th className="px-4 pb-8 font-normal">Ticker</th>
                            <th className="px-4 pb-8 font-normal">Valor atual</th>
                            <th className="px-4 pb-8 font-normal">Percentual</th>
                            <th className="px-4 pb-8 font-normal">Nota</th>
                            <th className="px-4 pb-8 font-normal">Quantidade</th>
                            <th className="px-4 pb-8 font-normal"></th>
                        </tr>
                    </thead>
                    <tbody className="text-white">
                        {assets.length === 0 ? (
                            <tr>
                                <td colSpan={7} className="py-8 text-center font-['Poppins'] text-sm text-[#808191]">
                                    Nenhum ativo encontrado nesta categoria.
                                </td>
                            </tr>
                        ) : (
                            assets.map((asset) => (
                                <tr key={asset.id} className="align-middle transition-colors hover:bg-white/[0.02]">
                                    <td className="px-4 py-3.5 font-['Poppins'] text-[12px] font-medium tracking-[0.3px]">{asset.tipo}</td>
                                    <td className="px-4 py-3.5 text-[14px] font-medium">{asset.ticker}</td>
                                    <td className="px-4 py-3.5 text-[14px] font-medium">{money.format(Number(asset.valorAtual || 0))}</td>
                                    <td className="px-4 py-3.5 text-[14px] font-medium">{percent.format(Number(asset.percentual || 0))}%</td>
                                    <td className="px-4 py-3.5 text-[14px] font-medium">{asset.nota ?? 0}</td>
                                    <td className="px-4 py-3.5 text-[14px]">{asset.quantidade}</td>
                                    <td className="px-4 py-3.5 text-right">
                                        <AssetMenu
                                            assetId={asset.id}
                                            onEdit={onEdit}
                                            onDelete={onDelete}
                                            onEvaluate={onEvaluate}
                                        />
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
