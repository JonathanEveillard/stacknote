import uvicorn
from fastapi import FastAPI
from fastapi.responses import HTMLResponse
import logging
PORT = 3008


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s: %(message)s",
)

logger = logging.getLogger(__name__)
app = FastAPI()
@app.get("/health",response_class=HTMLResponse)
async def printHealth():
    logger.info("A connection has been made.")
    return """
        <html>
            <head>
                <title>FastAPI HTML</title>
            </head>
            <body style="display:flex; justify-content:center; align-items:center; height:100%;">
                <div style="display:flex-column">
                    <h1>Stacknote server running!</h1>
                    <p>Please visit  localhost:3008/docs for API documentation</p>
                </div>
            </body>
        </html>
    """

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=PORT, reload=True)