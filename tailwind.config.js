/** @type {import('tailwindcss').Config} */
export default {
  content: [

    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",

  ],
  theme: {
    extend: {

      animation: {

        marqueeLeft: "marqueeLeft 30s linear infinite",
        marqueeRight: "marqueeRight 30s linear infinite",

      },

      keyframes: {

        marqueeLeft: {
          "0%": { transform: "translateX(0)" },
          "100%": { transform: "translateX(-50%)" },
        },

        marqueeRight: {
          "0%": { transform: "translateX(-50%)" },
          "100%": { transform: "translateX(0)" },
        },

      },

    },
  },
  plugins: [],
}
