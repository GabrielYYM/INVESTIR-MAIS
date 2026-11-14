import { Box, Pagination } from "@mui/material";
import { useListContext } from "react-admin";
export default function ResourcePagination() {
  const { page, perPage, total = 0, setPage } = useListContext();
  if (total <= perPage) return null;
  return <Box sx={{ py: 2, display: "flex", justifyContent: "end" }}><Pagination page={page} count={Math.ceil(total / perPage)} onChange={(_, value) => setPage(value)} color="primary" /></Box>;
}
