import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

export function Layout({ children }) {
    return (
        <main className="flex h-screen w-screen bg-black p-2 sm:p-4 overflow-hidden">
            <div className="flex h-full w-full flex-row overflow-hidden bg-[#1f1d2b] text-white shadow-2xl">
                <Sidebar />

                <div className="flex h-full min-w-0 flex-1 flex-col px-8 lg:px-12">
                    <Header />

                    <div className="min-h-0 flex-1 overflow-y-auto pb-10">
                        {children || <Outlet />}
                    </div>
                </div>
            </div>
        </main>
    );
}
