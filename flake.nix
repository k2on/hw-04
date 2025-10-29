{
  description = "JavaFX development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk
            # maven
            libGL
            gtk3
            glib
            xorg.libX11
            xorg.libXtst
            xorg.libXxf86vm
            cairo
            pango
            gdk-pixbuf
            atk
          ];
          
          shellHook = ''
            export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [
              pkgs.libGL
              pkgs.gtk3
              pkgs.glib
              pkgs.cairo
              pkgs.pango
              pkgs.gdk-pixbuf
              pkgs.atk
              pkgs.xorg.libX11
              pkgs.xorg.libXtst
              pkgs.xorg.libXxf86vm
            ]}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
            echo "LD_LIBRARY_PATH set for JavaFX"
          '';
        };
      }
    );
}
