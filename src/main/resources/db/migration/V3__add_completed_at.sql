-- タスクの完了（設計書 0004）。null なら未完了。既存の行は null のまま（未完了）
alter table tasks add column completed_at timestamptz;
