# Require any additional compass plugins here.
#
# Set this to the root of your project when deployed:
http_path = (environment == :production) ? "http://txtback.co/" : "/"
project_path = "src/main/webapp"
css_dir = "static/css"
sass_dir = "compass-hidden"
images_dir = "images"
javascripts_dir = "static/js"
fonts_dir = "static/css/fonts"

# To enable relative paths to assets via compass helper functions. Uncomment:
relative_assets = environment != :production
output_style = (environment == :production) ? :compressed : :expanded
