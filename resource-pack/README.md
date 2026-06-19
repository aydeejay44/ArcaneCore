# ArcaneCore Resource Pack

This folder is the editable resource pack for ArcaneCore.

Correct pack layout:

```text
resource-pack/
  pack.mcmeta
  assets/
```

When you zip the pack, zip the contents of this folder, not the folder itself.

From the ArcaneCore workspace:

```powershell
if (Test-Path .\ArcaneCoreResourcePack.zip) { Remove-Item -LiteralPath .\ArcaneCoreResourcePack.zip }
jar --create --no-manifest --file .\ArcaneCoreResourcePack.zip -C .\resource-pack pack.mcmeta -C .\resource-pack assets -C .\resource-pack README.md
```

The status font is here:

```text
assets/arcanesmp/font/status.json
```

The status font textures are here:

```text
assets/arcanesmp/textures/font/status/
```

Glyphs currently mapped:

```text
\uE101 = Ember
\uE102 = Frost
\uE103 = Breeze
\uE104 = Luck
\uE105 = Void
```

Plugin config for using these icons:

```yml
status-bar:
  resource-pack-icons: true
  resource-pack-font: arcanesmp:status
```

The plugin applies `arcanesmp:status` directly to the status-bar glyphs. Do
not add these glyphs to `assets/minecraft/font/default.json`; overriding the
default Minecraft font can conflict with other server resource packs.

If the pack is not installed on the client, set `resource-pack-icons: false`
so players see the normal emoji/text fallback instead of missing glyph boxes.
