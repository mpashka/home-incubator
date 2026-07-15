import { build, context } from "esbuild";
import { cp, mkdir, rm } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = dirname(fileURLToPath(import.meta.url));
const outdir = resolve(root, "dist");
const watch = process.argv.includes("--watch");

/** Each MV3 script bundle is emitted as a standalone IIFE. */
const entryPoints = {
  "injected": "src/injected/injected.ts",
  "content": "src/content/content.ts",
  "background": "src/background/service-worker.ts",
  // Emitted into dist/popup/ so popup/index.html can reference ./popup.js.
  "popup/popup": "src/popup/popup.ts",
  "options/options": "src/options/options.ts",
};

const options = {
  entryPoints: Object.fromEntries(
    Object.entries(entryPoints).map(([name, p]) => [name, resolve(root, p)]),
  ),
  outdir,
  bundle: true,
  format: "iife",
  target: "chrome114",
  sourcemap: watch ? "inline" : false,
  logLevel: "info",
};

/** Copy static assets (manifest, side panel html) verbatim into dist. */
async function copyStatic() {
  await cp(resolve(root, "manifest.json"), resolve(outdir, "manifest.json"));
  await cp(resolve(root, "src/popup/index.html"), resolve(outdir, "popup/index.html"));
  await cp(resolve(root, "src/options/index.html"), resolve(outdir, "options/index.html"));
}

await rm(outdir, { recursive: true, force: true });
await mkdir(resolve(outdir, "popup"), { recursive: true });
await mkdir(resolve(outdir, "options"), { recursive: true });

if (watch) {
  const ctx = await context(options);
  await ctx.watch();
  await copyStatic();
  console.log("watching…");
} else {
  await build(options);
  await copyStatic();
  console.log("build complete → dist/");
}
