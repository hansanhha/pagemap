import React, { useState, useEffect} from "react";

function CurrentTime() {
    const [time, setTime] = useState(new Date());

    useEffect(() => {
        const timer = setInterval(() => {
            setTime(new Date());
        }, 1000);

        return () => {
            clearInterval(timer);
        }
    }, []);

    return (
        <div>
            {time.toLocaleTimeString()}
        </div>
    );
}

export default CurrentTime;