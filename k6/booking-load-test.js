import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN;
const EVENT_ID = __ENV.EVENT_ID;

export const options = {
    stages: [
        { duration: '5s', target: 10 },
        { duration: '5s', target: 100 },
        { duration: '5s', target: 500 },
        { duration: '10s', target: 0 },
    ],
};

export default function () {

    const url = `${BASE_URL}/api/events/1/bookings`;

    const payload = JSON.stringify({
        quantity: 1
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0Iiwicm9sZSI6IkNVU1RPTUVSIiwiaWF0IjoxNzg5OTAwNjA4LCJleHAiOjE3ODk5Mjk0MDh9.VMhtgXPiBpGWQ7H7CDYyJwKUsxqTCwG8t6StAbNXfYw`,
        },
    };

    const response = http.post(url, payload, params);

    check(response, {
        'booking successful': (r) => r.status === 201,
    });
}