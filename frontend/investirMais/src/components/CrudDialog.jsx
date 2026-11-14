import { Button, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { CreateBase, EditBase, SaveButton, SimpleForm, Toolbar, useDelete, useNotify, useSaveContext } from "react-admin";
import { useLocation, useNavigate, useRevalidator } from "react-router";
import { zodResolver } from "@hookform/resolvers/zod";
import AppModal from "./AppModal.jsx";
import { Notice } from "./PageState.jsx";
function FormFrame({
  title,
  label,
  onClose,
  schema,
  defaults,
  record,
  error,
  alwaysEnable,
  children
}) {
  const {
    saving
  } = useSaveContext();
  return <AppModal title={title} onClose={onClose} busy={saving} width={640}>
    <SimpleForm resolver={zodResolver(schema)} defaultValues={defaults} record={record} mode="onSubmit" reValidateMode="onChange" sx={{
      p: 0
    }} toolbar={<Toolbar sx={{
      bgcolor: "transparent",
      px: 0,
      justifyContent: "end",
      gap: 1
    }}>
        <Button disabled={saving} onClick={onClose}>Cancelar</Button><SaveButton label={label} alwaysEnable={!saving && alwaysEnable} disabled={saving} />
      </Toolbar>}>
      {error && <Notice error>{error}</Notice>}{children}
    </SimpleForm>
  </AppModal>;
}
export default function CrudDialog({
  resource,
  item,
  meta,
  title,
  label,
  schema,
  defaults,
  transform,
  onClose,
  onSuccess,
  children
}) {
  const revalidator = useRevalidator();
  const navigate = useNavigate();
  const location = useLocation();
  const [error,setError] = useState("");
  const mutationOptions = {
    meta,
    onSuccess: async data => {
      await (onSuccess ? onSuccess(data) : revalidator.revalidate());
      onClose();
    },
    onError: error => {
      if (error.status === 401) navigate("/entrar?" + new URLSearchParams({
        retorno: location.pathname + location.search
      }), {
        replace: true
      });else setError(error.message);
    }
  };
  const Controller = item ? EditBase : CreateBase;
  return <Controller resource={resource} id={item?.id} disableAuthentication mutationMode="pessimistic" mutationOptions={mutationOptions} transform={transform} {...item ? {
    queryOptions: {
      meta,
      initialData: item,
      staleTime: Infinity
    }
  } : {}}>
    <FormFrame title={title} label={label} schema={schema} defaults={defaults} error={error} alwaysEnable={resource !== "users"} record={item ? {
      ...item,
      ...defaults
    } : undefined} onClose={onClose}>{children}</FormFrame>
  </Controller>;
}
export function DeleteRecordDialog({
  resource,
  item,
  meta,
  title,
  children,
  onClose,
  onSuccess
}) {
  const [remove, {
    isPending
  }] = useDelete();
  const notify = useNotify();
  const revalidator = useRevalidator();
  const submit = () => remove(resource, {
    id: item.id,
    previousData: item,
    meta
  }, {
    mutationMode: "pessimistic",
    onSuccess: async () => {
      await (onSuccess ? onSuccess() : revalidator.revalidate());
      onClose();
    },
    onError: error => notify(error.message, {
      type: "error"
    })
  });
  return <AppModal title={title} busy={isPending} onClose={onClose}>
    <Stack spacing={3}><Typography>{children}</Typography><Stack direction="row" spacing={1} sx={{
        justifyContent: "end"
      }}>
      <Button disabled={isPending} onClick={onClose}>Cancelar</Button><Button color="error" variant="contained" disabled={isPending} onClick={submit}>Confirmar exclusão</Button>
    </Stack></Stack>
  </AppModal>;
}
