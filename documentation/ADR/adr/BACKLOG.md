# Stacknote Product Backlog

## Overview

Stacknote is a free, open source desktop app that organizes your notes and lets you search and ask questions about them, fully offline. You point it at a folder, it indexes everything locally, suggests tags, groups related notes, and can propose a cleaner folder structure that it applies only with your approval. A local LLM answers questions with citations back to your files.

**Simplified:** An AI organizer and NotebookLM for your local files, private and free.

### Problems it solves

| Problem | Who has it | How Stacknote solves it |
| --- | --- | --- |
| Keyword search misses notes that use different words for the same idea | Students, engineers, anyone with years of notes | Semantic search over local embeddings finds notes by meaning |
| Knowledge is scattered across markdown, PDFs and screenshots | Anyone whose notes span several formats | One index over a watched folder; PDFs parsed, scans OCR'd (v2) |
| Notes pile up with no consistent tags or structure | Anyone with a large, messy vault | AI suggests tags and clusters related notes using the existing embeddings |
| Reorganizing hundreds of files by hand is tedious | Users with years of unsorted notes | AI proposes a folder structure; moves and renames happen only after approval, with undo |
| Cloud AI tools require uploading private notes | Privacy conscious users, people with sensitive work notes | Embeddings, vector DB and LLM all run on the device; nothing leaves the machine |
| Existing AI note tools cost a monthly subscription | Students, hobbyists | FOSS, no account, no cloud bill; uses the user's own hardware |
| Indexes go stale as notes change | Active note takers | File watcher re-indexes only changed files, by content hash |
| AI answers can't be trusted without sources | Anyone relying on answers for study or work | Every answer cites the source file and chunk it came from |

### Target users

- Students who want to query their lecture notes and readings
- Developers and engineers with large markdown or Obsidian-style vaults
- Privacy-focused users who won't send notes to a cloud service

## Work hierarchy (v1)

v1 delivers two outcomes, fully offline: cited answers over a local folder of notes, and AI-assisted organization of those notes. The hierarchy runs Initiative → Epic → Feature → User story → Task.

**Initiative:** Stacknote v1 — private, offline Q&A and organization for local notes.

### E1 — Project foundation

*Solves: a reliable base so every later epic builds and ships on all three OSes.*

- **F1.1 Repo and scaffolding**
    - US-1: As a contributor, I can clone the repo and run the app with one command, so onboarding is fast.
        - Scaffold Tauri v2 + React + TypeScript + Tailwind
        - Set up Cargo workspace (`app`, `core`, `indexer`)
        - Add README, CONTRIBUTING, license, issue templates
- **F1.2 CI pipeline**
    - US-2: As a maintainer, every PR is linted, tested and built automatically, so main stays green.
        - GitHub Actions: `cargo fmt`, `clippy`, `cargo test`, ESLint, Vitest
        - Build matrix for macOS (Metal), Windows and Linux (Vulkan), plus a CPU-only build
        - Cache llama.cpp C++ builds to keep CI times down

### E2 — File ingestion

*Solves: knowledge scattered across formats, and indexes that go stale.*

- **F2.1 Folder selection and initial scan**
    - US-3: As a user, I can choose a notes folder, so Stacknote indexes only what I want.
        - Folder picker via Tauri dialog
        - Recursive scan for `.md`, `.txt`, `.pdf`; ignore hidden and binary files
- **F2.2 Parsing and chunking**
    - US-4: As a user, my markdown, text and PDFs are all searchable, so I don't have to convert anything.
        - Markdown/text reader; PDF text extraction with `pdf-extract`
        - Chunker: heading-aware for markdown, fixed-size with overlap otherwise
- **F2.3 Live re-indexing**
    - US-5: As a user, edits and new files are picked up automatically, so search is always current.
        - `notify` watcher with debounce
        - SQLite table of path + content hash; re-index only changed files
        - Remove vectors when a file is deleted

### E3 — Embeddings and vector storage

*Solves: keyword search missing notes that use different words; privacy (no cloud embedding APIs).*

- **F3.1 Local embeddings**
    - US-6: As a user, my notes are embedded on my machine, so nothing is sent to a server.
        - Integrate `fastembed-rs` with bge-small; download model on first run
        - Batch embedding with progress events to the UI
- **F3.2 Vector store**
    - US-7: As a user, the index persists between launches, so I don't wait for a full re-index.
        - LanceDB table schema: chunk text, vector, file path, chunk offset
        - Upsert and delete by file

### E4 — Retrieval and chat

*Solves: finding answers by meaning and trusting them through citations.*

- **F4.1 Semantic search**
    - US-8: As a user, I can search by meaning and see the most relevant passages, so I find notes even when I forget the exact words.
        - Top-k vector query; return chunks with file and score
- **F4.2 Embedded LLM runtime**
    - US-9: As a user, answers are generated on my machine with nothing else installed, so the app just works.
        - Integrate `llama-cpp-2` behind an `LlmBackend` trait
        - Load the model once into managed state; run inference on a blocking thread
        - GPU backend selection (Metal, Vulkan) with automatic CPU fallback
- **F4.3 Cited Q&A**
    - US-10: As a user, I can ask a question and get an answer that cites the source files, so I can verify it.
        - Prompt template injecting retrieved chunks
        - Stream tokens to the UI over a Tauri channel
        - Map citations back to file paths
    - US-11: As a user, clicking a citation opens the source file, so I can read the full context.
        - Open file in the default app via Tauri shell API

### E5 — Desktop UI

*Solves: a non-technical user can use it without a terminal.*

- **F5.1 Search and chat views**
    - US-12: As a user, I have a search box and a chat panel in one window, so the main workflow is obvious.
        - Search results list; chat panel with streaming text
        - Citation chips under each answer
- **F5.2 Index status**
    - US-13: As a user, I can see indexing progress and errors, so I know when search is ready.
        - Status bar: files indexed, in progress, failed

### E6 — Settings and onboarding

*Solves: zero-cost, low-friction setup for non-technical users.*

- **F6.1 First-run model setup**
    - US-14: As a new user, I click one button and the app downloads the right AI model for my computer, so I never touch a terminal or install anything else.
        - Detect RAM, GPU support and free disk space
        - Recommend a tier (Lite ~1 GB, Standard ~2 GB, Quality ~4.5 GB); advanced picker for others
        - Download with progress, pause/resume (HTTP range requests) and SHA-256 verification
        - Pinned model manifest (URL, hash, size) in the repo; models hosted on Hugging Face
        - Plain-language errors; automatic fallback to a smaller tier or CPU
- **F6.2 Settings**
    - US-15: As a user, I can change the notes folder and the model, delete downloaded models, and trigger a full re-index.
        - Settings persisted in a local config file
        - Model manager showing size on disk

### E7 — Release and distribution

*Solves: free, trustworthy installs on every OS.*

- **F7.1 Release builds**
    - US-16: As a user, I can download an installer for my OS from GitHub Releases.
        - Tag-triggered release workflow producing `.dmg`, `.msi`, `.AppImage`/`.deb`
        - Code signing (macOS notarization, Windows signing) when certificates are available
- **F7.2 Auto-update**
    - US-17: As a user, the app updates itself, so I always have fixes.
        - Tauri updater plugin with signed update manifests

### E8 — Note organization

*Solves: messy vaults with no consistent tags or structure, and tedious manual reorganizing.*

- **F8.1 Auto-tagging**
    - US-18: As a user, Stacknote suggests tags for each note, so I can find related notes without tagging by hand.
        - LLM tag suggestions from note content
        - Store tags in SQLite; accept or reject per tag in the UI
        - Opt-in: write accepted tags to YAML front matter
- **F8.2 Related-note groups**
    - US-19: As a user, I see groups of related notes, so I discover connections across folders.
        - Note-level embedding (mean of chunk vectors)
        - Clustering with k-means or HDBSCAN; LLM names each group
        - Groups view in the UI
- **F8.3 Folder reorganization**
    - US-20: As a user, Stacknote proposes a folder structure and moves or renames files only after I approve, so I stay in control.
        - Generate a move/rename plan from groups
        - Preview screen: current path → proposed path, approve per item
        - Apply with an undo journal; never delete files
    - US-21: As a user, links between my notes keep working after a move.
        - Rewrite relative markdown links and wiki links to moved files
        - Pause the file watcher during apply to avoid a re-index storm

## Out of scope for v1 (later epics)

| Later epic | Problem it would solve | Why deferred |
| --- | --- | --- |
| OCR for screenshots and scans | Handwritten or screenshotted notes aren't searchable | Tesseract bundling per OS is the largest packaging cost |
| Optional Ollama backend | Power users want to reuse models they already have in Ollama | llama.cpp is built in; not needed for target users |
| Multi-hop / agentic retrieval | Questions that span several notes | Plain RAG must be solid first |
| More formats (DOCX, HTML, EPUB) | Notes kept outside markdown and PDF | Not needed to prove the core loop |
| Sync across devices | Index lives on one machine | Conflicts with the local-only, no-server goal |

### Open questions

- License: permissive (MIT or Apache-2.0) or copyleft (GPL-3.0)?
- Minimum hardware target for running a local LLM at acceptable speed?