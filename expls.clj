(require
 '[clojure.java.io :as io]
 '[clojure.string :as str]
 '[clojure.java.process :as process])

(import
 '[java.io File])

(defn get-terminal-width []
  (-> (process/exec "tput" "cols")
      str/trim
      parse-long))

(defn list-files [path]
  (->> (io/file path)
       .listFiles
       (seq)))

(defn dir-first-compare [^File a ^File b]
  (cond
    (and (.isDirectory a) (not (.isDirectory b))) -1
    (and (not (.isDirectory a)) (.isDirectory b)) 1
    :else (compare (.getName a) (.getName b))))

(defn format-filename [name]
  (let [max-len 16
        ellipsis "..."
        name-len (count name)]
    (str " "
         (if (str/starts-with? name ".") "\033[37m" "")
         (cond
           (> name-len max-len)
           (str (subs name 0 13) ellipsis)

           (<= name-len max-len)
           (let [pad-total (- max-len name-len)
                 pad-left (quot pad-total 2)
                 pad-right (- pad-total pad-left)]
             (str (apply str (repeat pad-left " "))
                  name
                  (apply str (repeat pad-right " ")))))
         (if (str/starts-with? name ".") "\033[0m" "")
         " ")))

(def folder-lines
  ["                  "
   " \033[0;33m🬹🬹🬹🬹🬹🬹🬿\033[0m          "
   " \033[0;93;43m🬭🬭🬭🬭🬭🬭🭄\033[0;93m████████🭌\033[0m "
   " \033[0;93m████████████████\033[0m "
   " \033[0;93m████████████████\033[0m "
   " \033[0;93m████████████████\033[0m "
   " \033[0;93m🭒██████████████🭝\033[0m "
   "                  "])

(def file-lines
  ["   ╭────────􀎧     "
   "   │ \033[36m━━━━━━ \033[0m│╲    "
   "   │ \033[36m━━━━━━ \033[0m╰─􀎥   "
   "   │ \033[36m━━━━━━━━ \033[0m│   "
   "   │ \033[36m━━━━━━━━ \033[0m│   "
   "   │ \033[36m━━━━━━━━ \033[0m│   "
   "   │ \033[36m━━━━━━━━ \033[0m│   "
   "   ╰──────────╯   "])

(defn -main [& args]
  (let [path (if (seq args) (first args) ".")
        files (list-files path)
        sorted (sort dir-first-compare files)]
    (doseq [row (partition-all (quot (get-terminal-width) 18) sorted)]
    ;; icons
      (doseq [line (range 0 8)]
        (doseq [^File f row]
          (if (.isDirectory f)
            (print (nth folder-lines line))
            (print (nth file-lines line))))
        (println))
    ;; file names
      (doseq [^File f row]
        (print (format-filename (.getName f))))
      (println)
      (println))))

(apply -main *command-line-args*)
