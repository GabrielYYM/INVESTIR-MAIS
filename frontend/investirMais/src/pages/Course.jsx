import { useState } from "react";
import { ArrowLeft, ExternalLink, Pencil, Play, Plus, Trash2, Video } from "lucide-react";
import { Link, NavLink, useLoaderData, useRouteLoaderData } from "react-router";
import EducationDialog from "../components/EducationDialog.jsx";
import { Pagination } from "../components/EducationParts.jsx";
import { canManageEducation, pageHref } from "../services/educationData.js";
import { externalMediaUrl, youtubeVideo } from "../services/youtube.js";

export { courseLoader } from "../services/educationData.js";

function LessonPlayer({ lesson }) {
  const [showPlayer, setShowPlayer] = useState(false);
  const video = youtubeVideo(lesson.mediaUrl);
  const link = video?.watchUrl ?? externalMediaUrl(lesson.mediaUrl);

  return (
    <article className="panel min-w-0 overflow-hidden">
      {video && showPlayer ? (
        <iframe
          className="aspect-video min-h-50 w-full border-0 bg-black"
          title={"Vídeo da aula " + lesson.title}
          src={video.embedUrl}
          referrerPolicy="strict-origin-when-cross-origin"
          allow="encrypted-media; picture-in-picture"
          allowFullScreen
        />
      ) : (
        <div className="grid min-h-44 place-items-center border-b border-white/5 bg-canvas px-5 py-8 sm:min-h-56">
          <span className="grid size-16 place-items-center rounded-2xl bg-amber-500/20 text-amber-400">
            <Video size={30} />
          </span>
        </div>
      )}
      <div className="space-y-3 p-5 sm:p-6">
        <p className="eyebrow">Videoaula</p>
        <h2 className="text-2xl font-bold break-words">{lesson.title}</h2>
        <p className="text-sm leading-relaxed break-words whitespace-pre-wrap text-muted">{lesson.description}</p>
        {link && (
          <div className="flex flex-wrap gap-2">
            <a className="button-primary" href={link} target="_blank" rel="noopener noreferrer">
              {video ? "Abrir no YouTube" : "Abrir vídeo"} <ExternalLink size={15} />
            </a>
            {video && !showPlayer && (
              <button className="button-secondary" onClick={() => setShowPlayer(true)}><Play size={16} /> Abrir player</button>
            )}
          </div>
        )}
        {video && <p className="text-xs leading-relaxed text-muted">Vídeos privados devem ser abertos no YouTube com a conta convidada pelo professor.</p>}
        {!link && <p className="text-sm text-muted">O endereço do vídeo está indisponível.</p>}
      </div>
    </article>
  );
}

export default function Course() {
  const { course, page, lesson } = useLoaderData();
  const user = useRouteLoaderData("app");
  const canManage = canManageEducation(user, course);
  const [editor, setEditor] = useState(null);
  const coursePath = "/educacao/" + course.id;

  return (
    <>
      <header className="space-y-3">
        <Link to="/educacao" className="inline-flex items-center gap-2 text-sm font-semibold text-brand hover:underline"><ArrowLeft size={16} /> Voltar ao catálogo</Link>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="max-w-2xl min-w-0">
            <p className="eyebrow">Curso</p>
            <h1 className="mt-2 text-3xl font-bold break-words">{course.name}</h1>
            <p className="mt-3 text-sm leading-relaxed break-words whitespace-pre-wrap text-muted">{course.description}</p>
          </div>
          {canManage && (
            <div className="flex flex-wrap gap-2">
              <button className="button-secondary" onClick={() => setEditor({ kind: "curso", item: course })}><Pencil size={15} /> Editar curso</button>
              <button className="button-danger" onClick={() => setEditor({ kind: "curso", item: course, remove: true })}><Trash2 size={15} /> Excluir curso</button>
            </div>
          )}
        </div>
      </header>

      <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(18rem,0.62fr)]">
        {lesson ? <LessonPlayer key={lesson.id} lesson={lesson} /> : (
          <p className="panel p-6 text-sm text-muted">{canManage ? "Adicione uma videoaula para começar." : "Este curso ainda não tem aulas cadastradas."}</p>
        )}
        <section className="panel min-w-0 space-y-4 p-4 sm:p-5" aria-labelledby="aulas-titulo">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="eyebrow">Conteúdo</p>
              <h2 id="aulas-titulo" className="mt-1 text-xl font-bold">Aulas <span className="ml-1 text-sm text-muted">{page.totalItems}</span></h2>
            </div>
            {canManage && <button className="button-primary" onClick={() => setEditor({ kind: "aula" })}><Plus size={16} /> Nova aula</button>}
          </div>
          {page.items.length > 0 && (
            <ul className="space-y-2">
              {page.items.map((item) => {
                const selected = item.id === lesson?.id;
                return (
                  <li key={item.id} className={"flex min-w-0 items-center gap-1 rounded-lg border p-1 " + (selected ? "border-brand/35 bg-brand/8" : "border-line bg-canvas")}>
                    <NavLink end to={pageHref(coursePath + "/" + item.id, page.number)}
                      aria-current={selected ? "true" : undefined}
                      className="flex min-w-0 flex-1 items-center gap-2 rounded-md p-2 text-sm font-semibold hover:text-brand"
                    >
                      <Play size={15} className={"shrink-0 " + (selected ? "text-brand" : "text-muted")} /> <span className="break-words">{item.title}</span>
                    </NavLink>
                    {canManage && (
                      <div className="flex shrink-0 gap-1 pr-1">
                        <button className="rounded-md p-2 text-muted hover:bg-raised hover:text-ink" aria-label={"Editar aula " + item.title} onClick={() => setEditor({ kind: "aula", item })}><Pencil size={15} /></button>
                        <button className="rounded-md p-2 text-muted hover:bg-negative/10 hover:text-negative" aria-label={"Excluir aula " + item.title} onClick={() => setEditor({ kind: "aula", item, remove: true })}><Trash2 size={15} /></button>
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
          <Pagination page={page} pathname={coursePath} />
        </section>
      </div>
      {editor && canManage && (
        <EducationDialog {...editor} onClose={() => setEditor(null)} />
      )}
    </>
  );
}
